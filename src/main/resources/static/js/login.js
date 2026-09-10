function configurarRecuperacion() {
    const boton = document.getElementById("abrir-recuperacion");
    const dialogo = document.getElementById("dialogo-recuperacion");

    if (!boton || !dialogo) {
        return;
    }

    boton.addEventListener("click", () => {
        if (!dialogo.open) {
            dialogo.showModal();
        }
    });

    dialogo.addEventListener("close", () => {
        boton.focus();
    });
}

if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", configurarRecuperacion);
} else {
    configurarRecuperacion();
}