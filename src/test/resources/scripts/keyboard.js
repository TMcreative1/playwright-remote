function modifiers(event) {
    let m = [];
    if (event.altKey)
        m.push("Alt");
    if (event.ctrlKey)
        m.push("Control");
    if (event.shiftKey)
        m.push("Shift");
    return `[${m.join(' ')}]`
}

function log(...args) {
    console.log.apply(console, args);
    window.result += `${args.join(" ")}\n`;
}

function getResult() {
    let tmp = window.result.trim();
    window.result = "";
    return tmp;
}

window.result = "";
let textarea = document.querySelector('textarea');
textarea.focus();
textarea.addEventListener('keydown', event => {
    log('Keydown:', event.key, event.code, event.which, modifiers(event));
});
textarea.addEventListener('keypress', event => {
    log('Keypress:', event.key, event.code, event.which, event.charCode, modifiers(event));
});
textarea.addEventListener('keyup', event => {
    log('Keyup:', event.key, event.code, event.which, modifiers(event));
});