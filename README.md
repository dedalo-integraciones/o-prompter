# O'Prompter Modern

**O'Prompter Modern** es una aplicación de teleprompter avanzada para Android diseñada especialmente para músicos, directores, oradores y creadores de contenido. Combina las funcionalidades clásicas de un apuntador de texto con herramientas especializadas para la notación musical y gestión local de archivos mediante el framework moderno de almacenamiento de Android.

---

## 🚀 Características Principales

### 1. Teleprompter Fluido y Control de Avance
- **Desplazamiento Suave:** Visualización clara y cómoda del texto con desplazamiento vertical.
- **Botón de Avance Flotante:** Un botón táctil flotante que permite avanzar página por página con un toque, o **mantener presionado y arrastrar** para reposicionarlo libremente en la pantalla según la comodidad del usuario.

### 2. Panel Especializado en Símbolos Musicales
Ideal para músicos y directores que necesitan leer partituras o letras con anotaciones musicales en tiempo real:
- **Alteraciones:** Sostenido ($\sharp$), Bemol ($\flat$), Becuadro ($\natural$).
- **Estructura y Ritmo:** Barras de compás ($|$), símbolos de repetición ($\%$, $\times$).
- **Valores de Notas:** Redonda ($\U+1D55D$), Blanca ($\U+1D55E$), Negra ($\U+1D55F$), Corchea ($\U+1D560$), Semicorchea ($\U+1D561$).
- **Claves y Acordes:** Clave de Sol ($\U+1D51E$), Maj7 ($\triangle$), Disminuido ($^{\circ}$), Semidisminuido ($\varnothing$).
- **Tipografía Dedicada:** Integración de la fuente **NotoMusic** para un renderizado impecable de la simbología musical.

### 3. Formato de Texto Estilo Markdown
Permite dar formato visual al texto de forma intuitiva durante la edición:
- **Negrita:** `*texto*`
- **Cursiva:** `_texto_`
- **Subrayado:** `+texto+`
- **Tachado:** `~texto~`

### 4. Gestión de Archivos Local (SAF - Storage Access Framework)
- **Selección de Carpeta de Trabajo:** Permite al usuario elegir un directorio persistente en el dispositivo para organizar sus documentos.
- **Carga y Guardado:** Explorador integrado para cargar archivos `.txt` existentes y guardar nuevas notas o ediciones de manera automática y segura.

### 5. Pantalla de Bienvenida (Splash Screen)
- Pantalla de inicio con enlace directo al sitio web oficial de [Dédalo Integraciones](https://dedalointegraciones.vercel.app/).

---

## 🛠️ Tecnologías y Arquitectura

- **Lenguaje:** Java (Android SDK).
- **UI / Vistas:** Activities nativas (`MainActivity`, `SplashActivity`), `ScrollView`, `EditText`, `TextView`, paneles personalizados.
- **Procesamiento de Texto:** `SpannableString`, `MarkdownParser` personalizado con expresiones regulares y `TypefaceSpan`.
- **Almacenamiento:** `DocumentsContract` / Storage Access Framework (SAF) y `SharedPreferences` para persistencia de la ruta de trabajo.
- **Tipografías:** Fuentes personalizadas (`notomusic.ttf`).

---

## 📄 Licencia

Este proyecto se distribuye bajo los términos de la **Licencia MIT**. 

```text
MIT License

Copyright (c) 2026 Dédalo Integraciones

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES / FOREIGN
LIABILITY WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
