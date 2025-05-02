async function loadJsonFiles(jsonFiles) {
    let groupedData = {};

    for (const file of jsonFiles) {
        try {
            const response = await fetch(file);
            let data = await response.json();

            if (!Array.isArray(data)) {
                data = [data];
            }

            const fileName = file.split('/').pop().replace('.json', '');
            const capitalizedTitle = fileName.charAt(0).toUpperCase() + fileName.slice(1);

            if (!groupedData[capitalizedTitle]) {
                groupedData[capitalizedTitle] = [];
            }

            groupedData[capitalizedTitle] = groupedData[capitalizedTitle].concat(data);
        } catch (error) {
            console.error(`Failed to load ${file}:`, error);
        }
    }

    return groupedData;
}

async function loadDashboard() {
    const dashboardContainer = document.getElementById("dashboard");
    const validRegex = /^[^\s]+\(\)$/;

    const jsonFiles = [
        "data/api/plugin.json",
        "data/api/api.json"
    ];

    const groupedData = await loadJsonFiles(jsonFiles);

    for (const [title, items] of Object.entries(groupedData)) {
        const titleElement = document.createElement("h3");
        titleElement.textContent = title;
        dashboardContainer.appendChild(titleElement);

        const filteredItems = items.filter(data => validRegex.test(data.name));
        filteredItems.sort((a, b) => a.name.localeCompare(b.name));

        filteredItems.forEach(data => {
            const box = document.createElement("div");
            box.className = "reflection-box";

            const link = document.createElement("a");
            link.href = data.path;
            link.className = "box-title";
            link.textContent = data.name;

            box.appendChild(link);
            dashboardContainer.appendChild(box);
        });
    }
}
