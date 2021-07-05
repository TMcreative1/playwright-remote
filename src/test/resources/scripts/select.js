window.result = {
    onInput: null,
    onChange: null,
    onBubblingChange: null,
    onBubblingInput: null
};

let select = document.querySelector('select');

function makeEmpty() {
    for (let i = select.options.length - 1; i >= 0; --i) {
        select.remove(i);
    }
}

function makeMultiple() {
    select.setAttribute('multiple', true);
}

select.addEventListener('input', () => {
    window.result.onInput = Array.from(select.querySelectorAll('option:checked')).map((option) => {
        return option.value;
    });
}, false)

select.addEventListener('change', () => {
    window.result.onChange = Array.from(select.querySelectorAll('option:checked')).map((option) => {
        return option.value;
    });
}, false)

document.body.addEventListener('input', () => {
    window.result.onBubblingInput = Array.from(select.querySelectorAll('option:checked')).map((option) => {
        return option.value;
    });
}, false)

document.body.addEventListener('change', () => {
    window.result.onBubblingChange = Array.from(select.querySelectorAll('option:checked')).map((option) => {
        return option.value;
    });
}, false)