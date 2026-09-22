/* Ejecutar con Node.js. Integración del modelo y handlers reales; no sustituye Android ni pruebas de navegador. */
const fs=require('fs'),vm=require('vm'),assert=require('node:assert/strict'),path=require('path');
const root=__dirname,results=[];
function context(){
 const nodes=new Map(),stored=new Map();
 const node=s=>{if(!nodes.has(s))nodes.set(s,{innerHTML:'',textContent:'',value:'',style:{},open:false,scrollTop:0,scrollHeight:100,classList:{toggle(){return true},add(){},remove(){}},setAttribute(){},removeAttribute(){},close(){this.open=false},showModal(){this.open=true},addEventListener(){}});return nodes.get(s)};
 const c=vm.createContext({console,Date,Intl,structuredClone,assert,document:{querySelector:node,querySelectorAll:()=>[],addEventListener(){}},localStorage:{getItem:k=>stored.get(k)||null,setItem:(k,v)=>stored.set(k,v)},setTimeout:()=>{},setInterval:()=>{},FormData:class{constructor(f){return Object.entries(f)}},FileReader:class{readAsDataURL(f){this.result='data:image/png;base64,'+Buffer.from(f.name||'test').toString('base64');this.onload()}},Buffer});
 for(const f of ['model.js','stories.js'])vm.runInContext(fs.readFileSync(path.join(root,f),'utf8'),c);
 let app=fs.readFileSync(path.join(root,'app.js'),'utf8');vm.runInContext(app.slice(0,app.indexOf("document.addEventListener(")),c);
 vm.runInContext(`
 S=seed();S.account='juan';
 const user=id=>S.account=id;
 const res=id=>S.reservations.find(r=>r.id===id);
 const req=id=>S.requests.find(r=>r.id===id);
 const tr=id=>S.transfers.find(t=>t.id===id);
 const at=t=>{S.time=typeof t==='number'?t:Date.parse(t);S.tick=Date.now()};
 const extra=()=>{S.accounts.push({id:'lina',name:'Lina',email:'lina@demo.test',password:'Demo1234',role:'deportista',active:true});return 'lina'};
 const page=(r,q=null)=>{R=r;Q=q;render();return $('#app').innerHTML};
 const submitForm=async(name,data,q=Q)=>{Q=q;return handleForm(name,data)};
 const reject=async fn=>{let failed=false;try{await fn()}catch(e){failed=true}assert(failed,'La operación debía rechazarse')};
 const accepted=()=>{user('negocio');approveRequest('s1');user('juan')};
 const transferAccepted=()=>{user('juan');joinTransfer('t1');user('negocio');approveTransfer('t1');user('juan')};
 const contains=(text,...parts)=>parts.forEach(p=>assert(text.includes(p),'Falta texto/control: '+p));
 const no=(text,...parts)=>parts.forEach(p=>assert(!text.includes(p),'Texto/control indebido: '+p));
 const refund=()=>{user('juan');cancelReservation('r1','Cambio de plan');return S.refunds[0]};
 const reviewSeed=()=>{S.reviews.push({id:'rv1',reservation:'r2',user:'juan',score:5,comment:'Excelente sesión'});return S.reviews[0]};
 const invite=async()=>{extra();user('juan');await action('inviteUser',{group:'g1',user:'lina'});return S.groups[0].invitations[0]};
 const imageFile={name:'foto.png',size:100,type:'image/png'};
 `,c);return c;
}
async function test(ids,label,code){const c=context();try{await vm.runInContext('(async()=>{'+code+'})()',c);results.push({criteria:ids.split(' '),label,status:'PASS'})}catch(e){results.push({criteria:ids.split(' '),label,status:'FAIL',error:e.message});}}
const suites=['access','discovery','reservations','transfer','community','mobile','business'];
(async()=>{for(const suite of suites){const register=require('./tests-'+suite+'.cjs');await register(test)}
 const hu=fs.readFileSync((process.argv[2] ? path.resolve(process.argv[2]) : path.join(root,'..','..','Sporthub.wiki-publicacion','Historias-de-usuario.md')),'utf8');let id,criteria=[];for(const l of hu.split(/\r?\n/)){const h=l.match(/^### (HU-[CDEA]\d+) · (.+)/);if(h)id=h[1];const ca=l.match(/^- \*\*(CA-\d+):\*\* (.+)/);if(ca)criteria.push({id:id+'/'+ca[1],description:ca[2]})}
 const matrix=criteria.map(c=>({...c,tests:results.filter(r=>r.criteria.includes(c.id)).map(r=>({label:r.label,status:r.status})),status:results.some(r=>r.criteria.includes(c.id)&&r.status==='FAIL')?'FAIL':results.some(r=>r.criteria.includes(c.id))?'PASS':'NOT_TESTED'}));
 const report={date:new Date().toISOString(),scope:'Integración aislada de modelo, handlers y HTML generado con mocks. Las aserciones visuales y Android se registran por separado.',tests:results.length,passed:results.filter(r=>r.status==='PASS').length,failed:results.filter(r=>r.status==='FAIL').length,criteria:matrix.length,covered:matrix.filter(r=>r.status==='PASS').length,results,matrix};
 fs.writeFileSync(path.join(root,'verificacion-completa.json'),JSON.stringify(report,null,2));console.log(JSON.stringify({tests:report.tests,passed:report.passed,failed:report.failed,criteria:report.criteria,covered:report.covered,failures:results.filter(r=>r.status==='FAIL'),missing:matrix.filter(r=>r.status==='NOT_TESTED').map(x=>x.id)},null,2));if(report.failed||report.covered!==report.criteria)process.exitCode=1;
})();
