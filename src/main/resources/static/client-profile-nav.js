function toggleClientProfileMenu(event) {
    event.stopPropagation();
    const dropdown = document.getElementById('clientProfileDropdown');
    if (dropdown) {
        dropdown.classList.toggle('active');
    }
}

document.addEventListener('click', function () {
    const dropdown = document.getElementById('clientProfileDropdown');
    if (dropdown) {
        dropdown.classList.remove('active');
    }
});