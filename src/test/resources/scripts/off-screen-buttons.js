window.addEventListener('DOMContentLoaded', () => {
    for (const button of Array.from(document.querySelectorAll('button')))
        button.addEventListener('click', () => console.log('button #' + button.textContent + ' clicked'), false);
}, false);