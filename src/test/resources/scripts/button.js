window.result = 'Was not clicked';
window.offsetX = undefined;
window.offsetY = undefined;
window.pageX = undefined;
window.pageY = undefined;
window.shiftKey = undefined;
window.pageX = undefined;
window.pageY = undefined;
window.bubbles = undefined;

document.querySelector('button').addEventListener('click', e => {
    window.result = 'Clicked';
    window.offsetX = e.offsetX;
    window.offsetY = e.offsetY;
    window.pageX = e.pageX;
    window.pageY = e.pageY;
    window.shiftKey = e.shiftKey;
    window.bubbles = e.bubbles;
    window.cancelable = e.cancelable;
    window.composed = e.composed;
}, false);