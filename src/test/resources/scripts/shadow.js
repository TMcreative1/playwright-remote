let h1 = null;
window.button = null;
let clicked = false;

window.addEventListener('DOMContentLoaded', () => {
    const shadowRoot = document.body.attachShadow({mode: 'open'});
    h1 = document.createElement('h1');
    h1.textContent = 'Hello Shadow DOM v1';
    window.button = document.createElement('button');
    window.button.textContent = 'Click';
    window.button.addEventListener('click', () => clicked = true);
    shadowRoot.appendChild(h1);
    shadowRoot.appendChild(button);
});