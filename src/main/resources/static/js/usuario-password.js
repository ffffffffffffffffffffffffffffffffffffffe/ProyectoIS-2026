const botonMostrar = document.getElementById("mostrar-password");
const nuevaPassword = document.getElementById("nuevaPassword");
const confirmacion = document.getElementById("confirmacion");

if (botonMostrar && nuevaPassword && confirmacion) {
    botonMostrar.addEventListener("click", () => {
        const mostrar = nuevaPassword.type === "password";

        nuevaPassword.type = mostrar ? "text" : "password";
        confirmacion.type = mostrar ? "text" : "password";

        botonMostrar.textContent = mostrar
            ? "Ocultar contraseñas"
            : "Mostrar contraseñas";

        botonMostrar.setAttribute("aria-pressed", String(mostrar));
    });
}