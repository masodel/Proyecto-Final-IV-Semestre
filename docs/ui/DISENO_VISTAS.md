# Diseño de las vistas — App de Fútbol (JavaFX)

Documento de referencia para implementar las 3 pantallas del proyecto. Cada pantalla está en HTML/CSS (abre `mockups/*.html` en un navegador para verlas). **El HTML es solo un modelo visual**: hay que reproducirlo en JavaFX (FXML + CSS), no copiar el HTML.

## Ventana y estructura común

- Ventana de escritorio de **1100 × 680 px** (mínimo), fondo `#F2F5F3`, fuente Inter con respaldo a Arial/System.
- **Barra lateral** izquierda de 200 px, fondo `#0F2A24`, texto blanco: marca "Fútbol App" (nombre provisional), tres opciones de navegación (Partidos, Posiciones, Favoritos; la activa con fondo `#1C4A3C` y texto en negrita) y, al pie, el estado de la caché ("Caché local · actualizada 10:42" / "PostgreSQL conectado") en `#A9C2B8`, 12 px.
- **Área de contenido** a la derecha, padding 24 × 28, separación vertical de 16 px.
- Encabezado de cada pantalla: título (22 px, negrita) a la izquierda; filtros y botón **Actualizar** a la derecha.
- Tarjetas blancas con borde `#DCE5E0` y esquinas de 10 px. Encabezados de tabla en `#EAF0ED`, texto 12 px negrita `#4A5B55`; filas de 44 px con línea `#E6EDE9`.
- Botón primario `#17775A` con texto blanco; botón secundario blanco con borde `#C5D2CC`. Altura mínima 44 px, esquinas de 8 px.
- Etiquetas (liga, estado): píldora con fondo `#E3EFE9` y texto `#0F5A43`; "En vivo" con fondo `#FDE7E4` y texto `#9A2417`.
- Zona de Champions League en Posiciones: franja azul `#2F6FDB` de 4 px a la izquierda de la fila.
- Todos los datos de los modelos son **de ejemplo**. El estadio aparece como `[ESTADIO]` (placeholder).

## Comportamiento esperado

- **Partidos:** filtrar por liga (Todas / LaLiga / Premier League / Champions League) y fecha; botón Actualizar trae datos de la API y los guarda en la base de datos; estrella para marcar favorito; doble clic en una fila abre el detalle en la pantalla Favoritos.
- **Posiciones:** elegir liga (LaLiga / Premier League / Champions League) y temporada; botón Actualizar; doble clic en un equipo lo agrega o quita de favoritos.
- **Favoritos y detalle:** lista a la izquierda con pestañas Partidos / Equipos; panel de detalle a la derecha (liga, fecha, estadio, estado, marcador, eventos, estadísticas de posesión, tiros y tiros a puerta); botón para agregar o quitar de favoritos; botón Actualizar detalle.
- Toda llamada a la API o a la base de datos corre en segundo plano (la pantalla no se congela) y muestra un indicador de carga. Los errores se muestran en alertas con mensaje legible.

---

## Pantalla 1 · Partidos

Archivo: `mockups/partidos.html`

```html
<!doctype html>
<html lang="es"><head><meta charset="utf-8"><title>Partidos</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"><style>
body{margin:0}
.win{width:1100px;height:680px;display:flex;font-family:Inter,Arial,sans-serif;color:#14221E;background:#F2F5F3;box-sizing:border-box;overflow:hidden}
aside{width:200px;background:#0F2A24;color:#fff;display:flex;flex-direction:column;padding:20px 12px;box-sizing:border-box;gap:4px;flex:none}
.brand{font-weight:700;font-size:18px;padding:4px 12px 20px}
.nav{display:block;padding:12px;border-radius:8px;color:#C9DAD3;font-size:14px;text-decoration:none}
.nav.on{background:#1C4A3C;color:#fff;font-weight:600}
.foot{margin-top:auto;font-size:12px;line-height:1.5;color:#A9C2B8;padding:12px}
main{flex:1;display:flex;flex-direction:column;padding:24px 28px;gap:16px;min-width:0;box-sizing:border-box}
h1{margin:0;font-size:22px}h2{margin:0;font-size:15px}
.bar{display:flex;gap:10px;align-items:center}
.btn{height:44px;padding:0 14px;border:1px solid #C5D2CC;background:#fff;border-radius:8px;font:500 14px Inter,Arial,sans-serif;color:#14221E}
.btn.p{background:#17775A;color:#fff;border-color:#17775A}
.card{background:#fff;border:1px solid #DCE5E0;border-radius:10px;overflow:hidden}
table{width:100%;border-collapse:collapse;font-size:14px}
th{text-align:left;font-weight:600;font-size:12px;color:#4A5B55;background:#EAF0ED;padding:10px 12px}
td{padding:4px 12px;border-top:1px solid #E6EDE9;height:44px}
.r{text-align:right}.c{text-align:center}
.tag{font-size:12px;padding:3px 8px;border-radius:99px;background:#E3EFE9;color:#0F5A43;font-weight:600}
.live{background:#FDE7E4;color:#9A2417}
.note{font-size:12px;color:#4A5B55}
.item{padding:12px 14px;border-top:1px solid #E6EDE9;font-size:14px;display:flex;justify-content:space-between;align-items:center}
.item.on{background:#E3EFE9;font-weight:600}
.bk{height:8px;border-radius:4px;background:#D6E2DC;display:flex;overflow:hidden}
.st{display:grid;grid-template-columns:40px 1fr 40px;gap:10px;align-items:center;font-size:13px}
</style></head><body>
<div class="win"><aside><div class="brand">Fútbol App</div>
<a class="nav on" href="#">Partidos</a><a class="nav " href="#">Posiciones</a><a class="nav " href="#">Favoritos</a>
<div class="foot">Caché local · actualizada 10:42<br>PostgreSQL conectado</div></aside><main>
<div class="bar"><h1>Partidos</h1><span style="flex:1"></span><button class="btn">Liga: Todas ▾</button><button class="btn">Fecha: 10/11/2026 ▾</button><button class="btn p">Actualizar</button></div>
<div class="card"><table><thead><tr><th>Hora</th><th>Liga</th><th>Local</th><th class="c">Marcador</th><th>Visitante</th><th>Estado</th><th class="c">Fav.</th></tr></thead><tbody>
<tr><td>14:00</td><td><span class="tag">Champions</span></td><td>Inter</td><td class="c"><b>1 - 0</b></td><td>Bayern</td><td><span class="tag live">En vivo 67'</span></td><td class="c"><button class="btn" aria-label="Marcar favorito" style="padding:0 12px">★</button></td></tr>
<tr><td>16:30</td><td><span class="tag">LaLiga</span></td><td>Real Madrid</td><td class="c"><b>2 - 1</b></td><td>Barcelona</td><td><span class="tag ">Finalizado</span></td><td class="c"><button class="btn" aria-label="Marcar favorito" style="padding:0 12px">★</button></td></tr>
<tr><td>18:00</td><td><span class="tag">Premier</span></td><td>Arsenal</td><td class="c"><b>—</b></td><td>Chelsea</td><td><span class="tag ">Programado</span></td><td class="c"><button class="btn" aria-label="Marcar favorito" style="padding:0 12px">☆</button></td></tr>
<tr><td>18:45</td><td><span class="tag">LaLiga</span></td><td>Sevilla</td><td class="c"><b>—</b></td><td>Betis</td><td><span class="tag ">Programado</span></td><td class="c"><button class="btn" aria-label="Marcar favorito" style="padding:0 12px">☆</button></td></tr>
<tr><td>20:00</td><td><span class="tag">Premier</span></td><td>Liverpool</td><td class="c"><b>—</b></td><td>Man. City</td><td><span class="tag ">Programado</span></td><td class="c"><button class="btn" aria-label="Marcar favorito" style="padding:0 12px">☆</button></td></tr>
<tr><td>21:00</td><td><span class="tag">Champions</span></td><td>PSG</td><td class="c"><b>—</b></td><td>Atlético</td><td><span class="tag ">Programado</span></td><td class="c"><button class="btn" aria-label="Marcar favorito" style="padding:0 12px">☆</button></td></tr>
</tbody></table></div>
<div class="bar"><span class="note">6 partidos · datos de ejemplo · doble clic en una fila abre el detalle</span><span style="flex:1"></span><span class="note">Cargando desde la API… (ProgressIndicator)</span></div>
</main></div></body></html>
```

---

## Pantalla 2 · Tablas de posiciones

Archivo: `mockups/posiciones.html`

```html
<!doctype html>
<html lang="es"><head><meta charset="utf-8"><title>Posiciones</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"><style>
body{margin:0}
.win{width:1100px;height:680px;display:flex;font-family:Inter,Arial,sans-serif;color:#14221E;background:#F2F5F3;box-sizing:border-box;overflow:hidden}
aside{width:200px;background:#0F2A24;color:#fff;display:flex;flex-direction:column;padding:20px 12px;box-sizing:border-box;gap:4px;flex:none}
.brand{font-weight:700;font-size:18px;padding:4px 12px 20px}
.nav{display:block;padding:12px;border-radius:8px;color:#C9DAD3;font-size:14px;text-decoration:none}
.nav.on{background:#1C4A3C;color:#fff;font-weight:600}
.foot{margin-top:auto;font-size:12px;line-height:1.5;color:#A9C2B8;padding:12px}
main{flex:1;display:flex;flex-direction:column;padding:24px 28px;gap:16px;min-width:0;box-sizing:border-box}
h1{margin:0;font-size:22px}h2{margin:0;font-size:15px}
.bar{display:flex;gap:10px;align-items:center}
.btn{height:44px;padding:0 14px;border:1px solid #C5D2CC;background:#fff;border-radius:8px;font:500 14px Inter,Arial,sans-serif;color:#14221E}
.btn.p{background:#17775A;color:#fff;border-color:#17775A}
.card{background:#fff;border:1px solid #DCE5E0;border-radius:10px;overflow:hidden}
table{width:100%;border-collapse:collapse;font-size:14px}
th{text-align:left;font-weight:600;font-size:12px;color:#4A5B55;background:#EAF0ED;padding:10px 12px}
td{padding:4px 12px;border-top:1px solid #E6EDE9;height:44px}
.r{text-align:right}.c{text-align:center}
.tag{font-size:12px;padding:3px 8px;border-radius:99px;background:#E3EFE9;color:#0F5A43;font-weight:600}
.live{background:#FDE7E4;color:#9A2417}
.note{font-size:12px;color:#4A5B55}
.item{padding:12px 14px;border-top:1px solid #E6EDE9;font-size:14px;display:flex;justify-content:space-between;align-items:center}
.item.on{background:#E3EFE9;font-weight:600}
.bk{height:8px;border-radius:4px;background:#D6E2DC;display:flex;overflow:hidden}
.st{display:grid;grid-template-columns:40px 1fr 40px;gap:10px;align-items:center;font-size:13px}
</style></head><body>
<div class="win"><aside><div class="brand">Fútbol App</div>
<a class="nav " href="#">Partidos</a><a class="nav on" href="#">Posiciones</a><a class="nav " href="#">Favoritos</a>
<div class="foot">Caché local · actualizada 10:42<br>PostgreSQL conectado</div></aside><main>
<div class="bar"><h1>Tablas de posiciones</h1><span style="flex:1"></span><button class="btn">Liga: LaLiga ▾</button><button class="btn">Temporada: 2026/27 ▾</button><button class="btn p">Actualizar</button></div>
<div class="card"><table><thead><tr><th class="c">#</th><th>Equipo</th><th class="r">PJ</th><th class="r">G</th><th class="r">E</th><th class="r">P</th><th class="r">GF</th><th class="r">GC</th><th class="r">DG</th><th class="r">Pts</th></tr></thead><tbody>
<tr><td style="border-left:4px solid #2F6FDB" class="c">1</td><td><b>Real Madrid</b></td><td class="r">11</td><td class="r">8</td><td class="r">2</td><td class="r">1</td><td class="r">24</td><td class="r">9</td><td class="r">+15</td><td class="r"><b>26</b></td></tr>
<tr><td style="border-left:4px solid #2F6FDB" class="c">2</td><td><b>Barcelona</b></td><td class="r">11</td><td class="r">8</td><td class="r">1</td><td class="r">2</td><td class="r">28</td><td class="r">12</td><td class="r">+16</td><td class="r"><b>25</b></td></tr>
<tr><td style="border-left:4px solid #2F6FDB" class="c">3</td><td><b>Atlético</b></td><td class="r">11</td><td class="r">7</td><td class="r">3</td><td class="r">1</td><td class="r">20</td><td class="r">8</td><td class="r">+12</td><td class="r"><b>24</b></td></tr>
<tr><td style="border-left:4px solid #2F6FDB" class="c">4</td><td><b>Athletic</b></td><td class="r">11</td><td class="r">6</td><td class="r">3</td><td class="r">2</td><td class="r">17</td><td class="r">10</td><td class="r">+7</td><td class="r"><b>21</b></td></tr>
<tr><td style="border-left:4px solid transparent" class="c">5</td><td><b>Villarreal</b></td><td class="r">11</td><td class="r">6</td><td class="r">2</td><td class="r">3</td><td class="r">21</td><td class="r">15</td><td class="r">+6</td><td class="r"><b>20</b></td></tr>
<tr><td style="border-left:4px solid transparent" class="c">6</td><td><b>Real Sociedad</b></td><td class="r">11</td><td class="r">5</td><td class="r">4</td><td class="r">2</td><td class="r">14</td><td class="r">9</td><td class="r">+5</td><td class="r"><b>19</b></td></tr>
<tr><td style="border-left:4px solid transparent" class="c">7</td><td><b>Betis</b></td><td class="r">11</td><td class="r">5</td><td class="r">3</td><td class="r">3</td><td class="r">16</td><td class="r">13</td><td class="r">+3</td><td class="r"><b>18</b></td></tr>
<tr><td style="border-left:4px solid transparent" class="c">8</td><td><b>Girona</b></td><td class="r">11</td><td class="r">4</td><td class="r">3</td><td class="r">4</td><td class="r">13</td><td class="r">14</td><td class="r">-1</td><td class="r"><b>15</b></td></tr>
</tbody></table></div>
<div class="bar"><span style="width:12px;height:12px;background:#2F6FDB;border-radius:3px"></span><span class="note">Clasificación a Champions League · datos de ejemplo · clic en un equipo para ver sus partidos y marcarlo favorito</span></div>
</main></div></body></html>
```

---

## Pantalla 3 · Favoritos y detalle

Archivo: `mockups/favoritos.html`

```html
<!doctype html>
<html lang="es"><head><meta charset="utf-8"><title>Favoritos y detalle</title>
<link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"><style>
body{margin:0}
.win{width:1100px;height:680px;display:flex;font-family:Inter,Arial,sans-serif;color:#14221E;background:#F2F5F3;box-sizing:border-box;overflow:hidden}
aside{width:200px;background:#0F2A24;color:#fff;display:flex;flex-direction:column;padding:20px 12px;box-sizing:border-box;gap:4px;flex:none}
.brand{font-weight:700;font-size:18px;padding:4px 12px 20px}
.nav{display:block;padding:12px;border-radius:8px;color:#C9DAD3;font-size:14px;text-decoration:none}
.nav.on{background:#1C4A3C;color:#fff;font-weight:600}
.foot{margin-top:auto;font-size:12px;line-height:1.5;color:#A9C2B8;padding:12px}
main{flex:1;display:flex;flex-direction:column;padding:24px 28px;gap:16px;min-width:0;box-sizing:border-box}
h1{margin:0;font-size:22px}h2{margin:0;font-size:15px}
.bar{display:flex;gap:10px;align-items:center}
.btn{height:44px;padding:0 14px;border:1px solid #C5D2CC;background:#fff;border-radius:8px;font:500 14px Inter,Arial,sans-serif;color:#14221E}
.btn.p{background:#17775A;color:#fff;border-color:#17775A}
.card{background:#fff;border:1px solid #DCE5E0;border-radius:10px;overflow:hidden}
table{width:100%;border-collapse:collapse;font-size:14px}
th{text-align:left;font-weight:600;font-size:12px;color:#4A5B55;background:#EAF0ED;padding:10px 12px}
td{padding:4px 12px;border-top:1px solid #E6EDE9;height:44px}
.r{text-align:right}.c{text-align:center}
.tag{font-size:12px;padding:3px 8px;border-radius:99px;background:#E3EFE9;color:#0F5A43;font-weight:600}
.live{background:#FDE7E4;color:#9A2417}
.note{font-size:12px;color:#4A5B55}
.item{padding:12px 14px;border-top:1px solid #E6EDE9;font-size:14px;display:flex;justify-content:space-between;align-items:center}
.item.on{background:#E3EFE9;font-weight:600}
.bk{height:8px;border-radius:4px;background:#D6E2DC;display:flex;overflow:hidden}
.st{display:grid;grid-template-columns:40px 1fr 40px;gap:10px;align-items:center;font-size:13px}
</style></head><body>
<div class="win"><aside><div class="brand">Fútbol App</div>
<a class="nav " href="#">Partidos</a><a class="nav " href="#">Posiciones</a><a class="nav on" href="#">Favoritos</a>
<div class="foot">Caché local · actualizada 10:42<br>PostgreSQL conectado</div></aside><main>
<div class="bar"><h1>Favoritos y detalle</h1><span style="flex:1"></span><button class="btn p">Actualizar detalle</button></div>
<div style="display:flex;gap:16px;flex:1;min-height:0">
<div class="card" style="width:290px;flex:none"><div class="bar" style="padding:12px"><button class="btn p" style="flex:1">Partidos</button><button class="btn" style="flex:1">Equipos</button></div>
<div class="item on"><span>Real Madrid 2 - 1 Barcelona</span><span>★</span></div>
<div class="item"><span>Inter 1 - 0 Bayern</span><span>★</span></div>
<div class="item"><span>Arsenal vs Chelsea</span><span>★</span></div>
<div class="item"><span>PSG vs Atlético</span><span>★</span></div></div>
<div class="card" style="flex:1;padding:22px;display:flex;flex-direction:column;gap:18px;overflow:hidden">
<div class="bar"><span class="tag">LaLiga</span><span class="note">10/11/2026 · 16:30 · Estadio [ESTADIO]</span><span style="flex:1"></span><span class="tag">Finalizado</span></div>
<div style="display:flex;justify-content:center;align-items:center;gap:28px;font-size:20px;font-weight:600"><span>Real Madrid</span><span style="font-size:40px;font-weight:700">2 - 1</span><span>Barcelona</span></div>
<div style="display:flex;gap:28px">
<div style="flex:1;display:flex;flex-direction:column;gap:10px"><h2>Eventos del partido</h2>
<div class="note" style="font-size:14px;color:#14221E">12' · Gol · Real Madrid</div>
<div class="note" style="font-size:14px;color:#14221E">34' · Tarjeta amarilla · Barcelona</div>
<div class="note" style="font-size:14px;color:#14221E">58' · Gol · Barcelona</div>
<div class="note" style="font-size:14px;color:#14221E">71' · Gol · Real Madrid</div></div>
<div style="flex:1;display:flex;flex-direction:column;gap:14px"><h2>Estadísticas</h2>
<div class="st"><span>54%</span><div class="bk"><div style="width:54%;background:#17775A"></div></div><span>46%</span></div>
<div class="st"><span>12</span><div class="bk"><div style="width:57%;background:#17775A"></div></div><span>9</span></div>
<div class="st"><span>5</span><div class="bk"><div style="width:62%;background:#17775A"></div></div><span>3</span></div>
<span class="note">Posesión · Tiros · Tiros a puerta</span></div></div>
<div class="bar" style="margin-top:auto"><button class="btn">Quitar de favoritos</button><span style="flex:1"></span><span class="note">Datos de ejemplo</span></div>
</div></div>
</main></div></body></html>
```
