window.result = {
    check: null,
    events: [],
};

let checkbox = document.querySelector('input');

const events = [
    'change',
    'click',
    'dblclick',
    'input',
    'mousedown',
    'mouseenter',
    'mouseleave',
    'mousemove',
    'mouseout',
    'mouseover',
    'mouseup',
];

for (let event of events) {
    checkbox.addEventListener(event, () => {
        if (['change', 'click', 'dblclick', 'input'].includes(event) === true) {
            result.check = checkbox.checked;
        }

        result.events.push(event);
    }, false);
}