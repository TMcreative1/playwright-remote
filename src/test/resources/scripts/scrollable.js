for (let i = 0; i < 100; i++) {
    let button = document.createElement("button");
    button.textContent = `${i}:not clicked`;
    button.id = `button-${i}`;
    button.onclick = () => button.textContent = "clicked";
    button.oncontextmenu = event => {
        event.preventDefault();
        button.textContent = "context menu";
    }
    document.body.appendChild(button)
    document.body.appendChild(document.createElement("br"))
}