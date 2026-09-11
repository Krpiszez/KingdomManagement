function goBack(fallback) {
    try {
        const referrer = document.referrer;
        const sameOrigin = referrer && new URL(referrer, window.location.href).origin === window.location.origin;
        if (sameOrigin && window.history.length > 1) {
            window.history.back();
            return;
        }
    } catch (e) {
        // Fall back to the supplied app route if the referrer cannot be evaluated.
    }
    window.location.href = fallback || '/';
}

function bindFileDrop(dropAreaId, fileInputId, fileNameId) {
    const dropArea = document.getElementById(dropAreaId);
    const fileInput = document.getElementById(fileInputId);
    const fileName = document.getElementById(fileNameId);
    if (!dropArea || !fileInput) return;

    const updateName = () => {
        fileName.textContent = fileInput.files && fileInput.files.length
            ? 'Selected: ' + fileInput.files[0].name
            : '';
    };

    fileInput.addEventListener('change', updateName);
    ['dragenter', 'dragover'].forEach(event => dropArea.addEventListener(event, e => {
        e.preventDefault();
        dropArea.classList.add('drag-over');
    }));
    ['dragleave', 'drop'].forEach(event => dropArea.addEventListener(event, e => {
        e.preventDefault();
        dropArea.classList.remove('drag-over');
    }));
    dropArea.addEventListener('drop', e => {
        if (e.dataTransfer.files.length) {
            fileInput.files = e.dataTransfer.files;
            updateName();
        }
    });
}
