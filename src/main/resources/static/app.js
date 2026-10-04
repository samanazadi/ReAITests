document.addEventListener("click", (event) => {
    const addButton = event.target.closest(".add-variant");
    if (addButton) {
        const form = addButton.closest("form");
        const template = form.querySelector("#variant-row-template");
        form.querySelector(".variant-rows").append(template.content.cloneNode(true));
        renumberVariantRows(form);
        return;
    }

    const removeButton = event.target.closest(".remove-variant");
    if (removeButton) {
        const form = removeButton.closest("form");
        removeButton.closest(".variant-row").remove();
        renumberVariantRows(form);
    }
});

function renumberVariantRows(form) {
    form.querySelectorAll(".variant-rows .variant-row").forEach((row, index) => {
        row.querySelectorAll("[name]").forEach((field) => {
            field.setAttribute("name", field.getAttribute("name").replace(/variants\[\d+]/, `variants[${index}]`));
        });
    });
}
