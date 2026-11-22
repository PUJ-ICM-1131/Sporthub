

import { readFileSync } from 'node:fs';
import { after, before, beforeEach, describe, it } from 'node:test';
import {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment
} from '@firebase/rules-unit-testing';
import {
  Bytes,
  deleteDoc,
  doc,
  getDoc,
  serverTimestamp,
  setDoc,
  Timestamp,
  updateDoc,
  writeBatch
} from 'firebase/firestore';

const PROYECTO = 'demo-sporthub';
const REGLAS = new URL('../../firestore.rules', import.meta.url);

const MONTO = 50000;
const POLITICA = { payMinutes: 30, refundHours: 24, refundPercent: 50 };

let entorno;

const usuarios = {
  juan: { nombre: 'Juan', email: 'juan@demo.test', rol: 'DEPORTISTA', telefono: null, negocioId: null, activa: true },
  camila: { nombre: 'Camila', email: 'camila@demo.test', rol: 'DEPORTISTA', telefono: null, negocioId: null, activa: true },
  otro: { nombre: 'Otro', email: 'otro@demo.test', rol: 'DEPORTISTA', telefono: null, negocioId: null, activa: true },
  ana: { nombre: 'Ana', email: 'ana@demo.test', rol: 'DEPORTISTA', telefono: null, negocioId: null, activa: false },
  nego: { nombre: 'Negocio', email: 'negocio@demo.test', rol: 'ESTABLECIMIENTO', telefono: null, negocioId: 'b1', activa: true },
  admin: { nombre: 'Admin', email: 'admin@demo.test', rol: 'ADMINISTRADOR', telefono: null, negocioId: null, activa: true }
};

function postulante(usuarioId, estado, aceptado) {
  return { id: `st_${usuarioId}`, usuarioId, estado, deadline: null, aceptadoPorPropietario: aceptado };
}

function reserva(usuarioId, enTransferencia) {
  return {
    usuarioId,
    actividadId: 'a1',
    negocioId: 'b1',
    fecha: '2027-01-15',
    hora: '10:00',
    duracionHoras: 1,
    precio: MONTO,
    estado: 'CONFIRMADA',
    pagoId: 'p1',
    cerrada: false,
    politica: POLITICA,
    llegada: false,
    fotoLlegada: null,
    enTransferencia,
    creadaEn: Timestamp.now()
  };
}

async function sembrar() {
  await entorno.withSecurityRulesDisabled(async (contexto) => {
    const db = contexto.firestore();
    const poner = (ruta, datos) => setDoc(doc(db, ruta), datos);

    for (const [uid, perfil] of Object.entries(usuarios)) await poner(`usuarios/${uid}`, perfil);

    await poner('negocios/b1', { nombre: 'Club Demo', estado: 'VALIDADO', descripcion: '', contacto: '', direccion: '' });
    await poner('actividades/a1', { negocioId: 'b1', nombre: 'Cancha', capacidad: 2, precio: MONTO, fotos: [] });

    await poner('chats/juan_camila', { miembros: ['juan', 'camila'], comunidadId: null, creadoEn: Timestamp.now() });
    await poner('chats/juan_nego', { miembros: ['juan', 'nego'], comunidadId: null, creadoEn: Timestamp.now() });
    await poner('comunidades/c1', { nombre: 'Comunidad', adminId: 'juan', miembros: ['juan', 'camila'], descripcion: '', foto: null });

    const encuentro = (origenId, tipo, creadorId) => ({
      origenTipo: tipo, origenId, creadorId, estado: 'ABIERTO', punto: null, sesion: 's1', creadoEn: Timestamp.now()
    });
    await poner('encuentros/e_juan_camila', encuentro('juan_camila', 'CHAT', 'juan'));
    await poner('encuentros/e_juan_nego', encuentro('juan_nego', 'CHAT', 'juan'));
    await poner('encuentros/e_c1', encuentro('c1', 'COMUNIDAD', 'camila'));

    const posicion = (instante) => ({ lat: 4.65, lng: -74.08, precision: 10, actualizadoEn: instante });
    await poner('encuentros/e_juan_camila/sesiones/s1/posiciones/juan', posicion(Timestamp.now()));
    await poner(
      'encuentros/e_juan_camila/sesiones/s1/posiciones/camila',
      posicion(Timestamp.fromMillis(Date.now() - 10 * 60 * 1000))
    );

    await poner('reservas/r1', reserva('juan', true));
    await poner('pagos/p1', { usuarioId: 'juan', monto: MONTO, estado: 'EXITOSO', medio: 'TARJETA', reservaId: 'r1', transferenciaId: '', negocioId: 'b1' });

    await poner('transferencias/t1', {
      reservaId: 'r1', propietarioId: 'juan', pagoId: 'p1', estado: 'TRAMITE', negocioId: 'b1', version: 1,
      postulantes: [postulante('camila', 'PAGO', true)], postulantesUids: ['camila']
    });
    await poner('autorizacionesTransferencia/t1', {
      compradorId: 'camila', propietarioId: 'juan', reservaId: 'r1', negocioId: 'b1',
      indice: 0, deadline: Date.now() + 30 * 60 * 1000, monto: MONTO
    });

    await poner('transferencias/t2', {
      reservaId: 'r1', propietarioId: 'juan', pagoId: 'p1', estado: 'PUBLICADA', negocioId: 'b1', version: 0,
      postulantes: [], postulantesUids: []
    });

    await poner('transferencias/t3', {
      reservaId: 'r1', propietarioId: 'juan', pagoId: 'p1', estado: 'TRAMITE', negocioId: 'b1', version: 1,
      postulantes: [postulante('camila', 'REVISION', true)], postulantesUids: ['camila']
    });

    await poner('fotos/h1', { datos: Bytes.fromUint8Array(new Uint8Array([1, 2, 3])), subidaPor: 'juan', creadaEn: Timestamp.now(), reservaId: null });
    await poner('fotosPrivadas/hp1', { datos: Bytes.fromUint8Array(new Uint8Array([1, 2, 3])), subidaPor: 'juan', creadaEn: Timestamp.now(), reservaId: 'r1' });
    await poner('reembolsos/x1', { usuarioId: 'juan', negocioId: 'b1', reservaId: 'r1', pagoId: 'p1', monto: 25000, estado: 'PENDIENTE', motivo: 'Transferencia' });
  });
}

const como = (uid) => entorno.authenticatedContext(uid).firestore();
const sinSesion = () => entorno.unauthenticatedContext().firestore();

before(async () => {
  entorno = await initializeTestEnvironment({
    projectId: PROYECTO,
    firestore: {
      rules: readFileSync(REGLAS, 'utf8'),
      host: process.env.FIRESTORE_EMULATOR_HOST?.split(':')[0] ?? '127.0.0.1',
      port: Number(process.env.FIRESTORE_EMULATOR_HOST?.split(':')[1] ?? 8080)
    }
  });
});

after(async () => {
  await entorno?.cleanup();
});

beforeEach(async () => {
  await entorno.clearFirestore();
  await sembrar();
});

describe('Encuentros: quién puede entrar', () => {
  it('un deportista del chat lee el encuentro', async () => {
    await assertSucceeds(getDoc(doc(como('juan'), 'encuentros/e_juan_camila')));
  });

  it('un establecimiento del chat no lee el encuentro', async () => {
    await assertFails(getDoc(doc(como('nego'), 'encuentros/e_juan_nego')));
  });

  it('el administrador de la aplicación no lee el encuentro', async () => {
    await assertFails(getDoc(doc(como('admin'), 'encuentros/e_juan_camila')));
  });

  it('una cuenta ajena al chat no lee el encuentro', async () => {
    await assertFails(getDoc(doc(como('otro'), 'encuentros/e_juan_camila')));
  });

  it('un deportista miembro de la comunidad lee su encuentro', async () => {
    await assertSucceeds(getDoc(doc(como('camila'), 'encuentros/e_c1')));
  });

  it('sin sesión no se lee nada', async () => {
    await assertFails(getDoc(doc(sinSesion(), 'encuentros/e_juan_camila')));
  });
});

describe('Encuentros: posiciones compartidas', () => {
  const ruta = 'encuentros/e_juan_camila/sesiones/s1/posiciones';

  it('cada persona publica su propia posición con la hora del servidor', async () => {
    await assertSucceeds(setDoc(doc(como('camila'), `${ruta}/camila`), {
      lat: 4.6, lng: -74.1, precision: 8, actualizadoEn: serverTimestamp()
    }));
  });

  it('nadie publica la posición de otra persona', async () => {
    await assertFails(setDoc(doc(como('camila'), `${ruta}/juan`), {
      lat: 4.6, lng: -74.1, precision: 8, actualizadoEn: serverTimestamp()
    }));
  });

  it('la hora tiene que ser la del servidor', async () => {
    await assertFails(setDoc(doc(como('camila'), `${ruta}/camila`), {
      lat: 4.6, lng: -74.1, precision: 8, actualizadoEn: Timestamp.fromMillis(Date.now() - 60 * 1000)
    }));
  });

  it('no se publica en una sesión distinta a la vigente', async () => {
    await assertFails(setDoc(doc(como('camila'), 'encuentros/e_juan_camila/sesiones/s0/posiciones/camila'), {
      lat: 4.6, lng: -74.1, precision: 8, actualizadoEn: serverTimestamp()
    }));
  });

  it('una posición reciente se lee', async () => {
    await assertSucceeds(getDoc(doc(como('camila'), `${ruta}/juan`)));
  });

  it('una posición de hace diez minutos ya no se lee', async () => {
    await assertFails(getDoc(doc(como('juan'), `${ruta}/camila`)));
  });

  it('una cuenta ajena no lee posiciones aunque sepa el identificador', async () => {
    await assertFails(getDoc(doc(como('otro'), `${ruta}/juan`)));
  });

  it('cada quien retira su propia posición, incluso tras perder el acceso', async () => {
    await assertSucceeds(deleteDoc(doc(como('juan'), `${ruta}/juan`)));
  });

  it('nadie borra la posición de otra persona', async () => {
    await assertFails(deleteDoc(doc(como('camila'), `${ruta}/juan`)));
  });
});

describe('Encuentros: punto de reunión y cierre', () => {
  it('cualquier participante propone el punto', async () => {
    await assertSucceeds(updateDoc(doc(como('camila'), 'encuentros/e_juan_camila'), {
      punto: { lat: 4.6, lng: -74.1, autorId: 'camila' }
    }));
  });

  it('el punto se guarda a nombre de quien lo propone', async () => {
    await assertFails(updateDoc(doc(como('camila'), 'encuentros/e_juan_camila'), {
      punto: { lat: 4.6, lng: -74.1, autorId: 'juan' }
    }));
  });

  it('quien abrió el encuentro lo finaliza', async () => {
    await assertSucceeds(updateDoc(doc(como('juan'), 'encuentros/e_juan_camila'), { estado: 'FINALIZADO' }));
  });

  it('otro participante no finaliza un encuentro de chat', async () => {
    await assertFails(updateDoc(doc(como('camila'), 'encuentros/e_juan_camila'), { estado: 'FINALIZADO' }));
  });

  it('el administrador de la comunidad finaliza su encuentro', async () => {
    await assertSucceeds(updateDoc(doc(como('juan'), 'encuentros/e_c1'), { estado: 'FINALIZADO' }));
  });
});

describe('Perfil', () => {
  it('se edita el propio nombre', async () => {
    await assertSucceeds(updateDoc(doc(como('juan'), 'usuarios/juan'), { nombre: 'Juan P.' }));
  });

  it('no se cambia el rol', async () => {
    await assertFails(updateDoc(doc(como('juan'), 'usuarios/juan'), { rol: 'ESTABLECIMIENTO' }));
  });

  it('no se asigna un negocio al propio perfil', async () => {
    await assertFails(updateDoc(doc(como('juan'), 'usuarios/juan'), { negocioId: 'b1' }));
  });

  it('no se edita el perfil de otra persona', async () => {
    await assertFails(updateDoc(doc(como('camila'), 'usuarios/juan'), { nombre: 'Otro' }));
  });
});

describe('Transferencias', () => {
  it('una persona interesada se inscribe en una publicación vigente', async () => {
    await assertSucceeds(updateDoc(doc(como('camila'), 'transferencias/t2'), {
      version: 1,
      postulantes: [postulante('camila', 'REVISION', false)],
      postulantesUids: ['camila']
    }));
  });

  it('al inscribirse no se puede llegar aceptada por el propietario', async () => {
    await assertFails(updateDoc(doc(como('camila'), 'transferencias/t2'), {
      version: 1,
      postulantes: [postulante('camila', 'REVISION', true)],
      postulantesUids: ['camila']
    }));
  });

  it('el propietario no emite la autorización de pago', async () => {
    await assertFails(setDoc(doc(como('juan'), 'autorizacionesTransferencia/t3'), {
      compradorId: 'camila', propietarioId: 'juan', reservaId: 'r1', negocioId: 'b1',
      indice: 0, deadline: Date.now() + 30 * 60 * 1000, monto: MONTO
    }));
  });

  it('el establecimiento emite la autorización de pago', async () => {
    await assertSucceeds(setDoc(doc(como('nego'), 'autorizacionesTransferencia/t3'), {
      compradorId: 'camila', propietarioId: 'juan', reservaId: 'r1', negocioId: 'b1',
      indice: 0, deadline: Date.now() + 30 * 60 * 1000, monto: MONTO
    }));
  });

  it('la autorización no puede llevar un monto distinto al de la reserva', async () => {
    await assertFails(setDoc(doc(como('nego'), 'autorizacionesTransferencia/t3'), {
      compradorId: 'camila', propietarioId: 'juan', reservaId: 'r1', negocioId: 'b1',
      indice: 0, deadline: Date.now() + 30 * 60 * 1000, monto: 1
    }));
  });

  it('no se cambia el titular de una reserva sin pagar la transferencia', async () => {
    await assertFails(updateDoc(doc(como('camila'), 'reservas/r1'), {
      usuarioId: 'camila', pagoId: 'p_t_t1', llegada: false, fotoLlegada: null, enTransferencia: false
    }));
  });

  it('completar la transferencia funciona en un solo lote con pago y autorización', async () => {
    const db = como('camila');
    const lote = writeBatch(db);
    lote.set(doc(db, 'pagos/p_t_t1'), {
      usuarioId: 'camila', monto: MONTO, estado: 'EXITOSO', medio: 'TARJETA',
      reservaId: 'r1', transferenciaId: 't1', negocioId: 'b1'
    });
    lote.update(doc(db, 'reservas/r1'), {
      usuarioId: 'camila', pagoId: 'p_t_t1', llegada: false, fotoLlegada: null, enTransferencia: false
    });
    lote.update(doc(db, 'transferencias/t1'), {
      version: 2, estado: 'COMPLETADA',
      postulantes: [postulante('camila', 'ATENDIDA', true)], postulantesUids: ['camila']
    });
    await assertSucceeds(lote.commit());
  });

  it('sin autorización el mismo lote se rechaza', async () => {
    const db = como('camila');
    const lote = writeBatch(db);
    lote.set(doc(db, 'pagos/p_t_t3'), {
      usuarioId: 'camila', monto: MONTO, estado: 'EXITOSO', medio: 'TARJETA',
      reservaId: 'r1', transferenciaId: 't3', negocioId: 'b1'
    });
    lote.update(doc(db, 'reservas/r1'), {
      usuarioId: 'camila', pagoId: 'p_t_t3', llegada: false, fotoLlegada: null, enTransferencia: false
    });
    lote.update(doc(db, 'transferencias/t3'), {
      version: 2, estado: 'COMPLETADA',
      postulantes: [postulante('camila', 'ATENDIDA', true)], postulantesUids: ['camila']
    });
    await assertFails(lote.commit());
  });

  it('una persona ajena no completa la transferencia de otra', async () => {
    const db = como('otro');
    const lote = writeBatch(db);
    lote.set(doc(db, 'pagos/p_t_t1'), {
      usuarioId: 'otro', monto: MONTO, estado: 'EXITOSO', medio: 'TARJETA',
      reservaId: 'r1', transferenciaId: 't1', negocioId: 'b1'
    });
    lote.update(doc(db, 'reservas/r1'), {
      usuarioId: 'otro', pagoId: 'p_t_t1', llegada: false, fotoLlegada: null, enTransferencia: false
    });
    lote.update(doc(db, 'transferencias/t1'), {
      version: 2, estado: 'COMPLETADA',
      postulantes: [postulante('otro', 'ATENDIDA', true)], postulantesUids: ['otro']
    });
    await assertFails(lote.commit());
  });
});

describe('Fotografías', () => {
  it('una fotografía compartida se crea a nombre propio', async () => {
    await assertSucceeds(setDoc(doc(como('camila'), 'fotos/h2'), {
      datos: Bytes.fromUint8Array(new Uint8Array([4, 5])), subidaPor: 'camila', creadaEn: serverTimestamp(), reservaId: null
    }));
  });

  it('no se sube una fotografía a nombre de otra persona', async () => {
    await assertFails(setDoc(doc(como('camila'), 'fotos/h3'), {
      datos: Bytes.fromUint8Array(new Uint8Array([4, 5])), subidaPor: 'juan', creadaEn: serverTimestamp(), reservaId: null
    }));
  });

  it('una fotografía existente no se sobrescribe', async () => {
    await assertFails(setDoc(doc(como('camila'), 'fotos/h1'), {
      datos: Bytes.fromUint8Array(new Uint8Array([9])), subidaPor: 'camila', creadaEn: serverTimestamp(), reservaId: null
    }));
  });

  it('la foto de llegada solo la sube el titular de la reserva', async () => {
    await assertFails(setDoc(doc(como('camila'), 'fotosPrivadas/hp2'), {
      datos: Bytes.fromUint8Array(new Uint8Array([4, 5])), subidaPor: 'camila', creadaEn: serverTimestamp(), reservaId: 'r1'
    }));
    await assertSucceeds(setDoc(doc(como('juan'), 'fotosPrivadas/hp3'), {
      datos: Bytes.fromUint8Array(new Uint8Array([4, 5])), subidaPor: 'juan', creadaEn: serverTimestamp(), reservaId: 'r1'
    }));
  });

  it('la foto de llegada la ven el titular y su establecimiento, nadie más', async () => {
    await assertSucceeds(getDoc(doc(como('juan'), 'fotosPrivadas/hp1')));
    await assertSucceeds(getDoc(doc(como('nego'), 'fotosPrivadas/hp1')));
    await assertFails(getDoc(doc(como('camila'), 'fotosPrivadas/hp1')));
  });
});

describe('Reembolsos', () => {
  it('quien recibe el reembolso lo consulta', async () => {
    await assertSucceeds(getDoc(doc(como('juan'), 'reembolsos/x1')));
  });

  it('una tercera persona no consulta el reembolso', async () => {
    await assertFails(getDoc(doc(como('camila'), 'reembolsos/x1')));
  });

  it('el establecimiento resuelve el reembolso', async () => {
    await assertSucceeds(updateDoc(doc(como('nego'), 'reembolsos/x1'), { estado: 'PAGADO' }));
  });

  it('el beneficiario no se paga su propio reembolso', async () => {
    await assertFails(updateDoc(doc(como('juan'), 'reembolsos/x1'), { estado: 'PAGADO' }));
  });
});
