document.addEventListener('DOMContentLoaded', function () {
    function generatePalette(amount) {
        let result = [];
        let hueStep = 360 / amount;
        for (let i = 0; i < amount; ++i) {
            result.push(`hsl(${hueStep * i}, 100%, 90%)`);
        }
        return result;
    }

    let palette = generatePalette(100);
    for (let i = 0; i < 200; ++i) {
        let box = document.createElement('div');
        box.classList.add('box');
        box.style.setProperty('background-color', palette[i % palette.length]);
        let x = i;
        do {
            let digit = x % 10;
            x = (x / 10) | 0;
            let img = document.createElement('img');
            img.src = `./images/digits/${digit}.png`;
            box.insertBefore(img, box.firstChild);
        } while (x);
        document.body.appendChild(box)
    }
});