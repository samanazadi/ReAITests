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
        return;
    }

    const deleteButton = event.target.closest(".delete-product");
    if (deleteButton) {
        const dialog = document.querySelector("#delete-dialog");
        dialog.querySelector(".delete-product-title").textContent = deleteButton.dataset.productTitle;
        dialog.querySelector("#confirm-delete").dataset.productId = deleteButton.dataset.productId;
        dialog.open = true;
        return;
    }

    const confirmButton = event.target.closest("#confirm-delete");
    if (confirmButton) {
        const productId = confirmButton.dataset.productId;
        htmx.ajax("DELETE", `/products/${productId}`, {
            source: confirmButton,
            target: `#product-${productId}`,
            swap: "outerHTML",
        }).finally(() => {
            confirmButton.closest("wa-dialog").open = false;
        });
    }
});

function renumberVariantRows(form) {
    form.querySelectorAll(".variant-rows .variant-row").forEach((row, index) => {
        row.querySelectorAll("[name]").forEach((field) => {
            field.setAttribute("name", field.getAttribute("name").replace(/variants\[\d+]/, `variants[${index}]`));
        });
    });
}
