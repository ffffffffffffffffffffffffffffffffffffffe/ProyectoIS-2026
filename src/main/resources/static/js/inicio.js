const botonNavegacion = document.getElementById("alternar-navegacion");
const navegacion = document.getElementById("navegacion-inicio");
const pantallaPequena = window.matchMedia("(max-width: 800px)");

if (botonNavegacion && navegacion) {
    function cambiarEstado(abierto) {
        navegacion.hidden = !abierto;
        botonNavegacion.setAttribute("aria-expanded", String(abierto));
        botonNavegacion.textContent = abierto
            ? "Ocultar menú"
            : "Mostrar menú";
    }

    function adaptarMenu() {
        botonNavegacion.hidden = !pantallaPequena.matches;
        cambiarEstado(!pantallaPequena.matches);
    }

    botonNavegacion.addEventListener("click", () => {
        cambiarEstado(navegacion.hidden);
    });

    pantallaPequena.addEventListener("change", adaptarMenu);

    adaptarMenu();
}
