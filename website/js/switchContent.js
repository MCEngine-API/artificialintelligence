function switchContent(type) {
    const codeDiv = document.getElementById('codeContent');
    const usageDiv = document.getElementById('usageContent');

    if (type === 'code') {
        codeDiv.style.display = 'block';
        usageDiv.style.display = 'none';
    } else if (type === 'usage') {
        codeDiv.style.display = 'none';
        usageDiv.style.display = 'block';
    }
}
