<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Liste des Signalements</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        body { padding: 20px; background-color: #f8f9fa; }
        .table-container { background: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
        .badge-nouveau { background-color: #0dcaf0; }
        .badge-en-cours { background-color: #ffc107; color: #000; }
        .badge-resolu { background-color: #198754; }
    </style>
</head>
<body>

<div class="container">
    <div class="table-container">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2>Liste des Signalements</h2>
            <a href="/signalements/nouveau" class="btn btn-success">Nouveau Signalement</a>
        </div>
        
        <table class="table table-hover">
            <thead class="table-dark">
                <tr>
                    <th>Date</th>
                    <th>Utilisateur</th>
                    <th>Latitude</th>
                    <th>Longitude</th>
                    <th>Statut</th>
                    <th>Actions</th>
                </tr>
            </thead>
            <tbody id="signalementsTableBody">
                <!-- Rempli par JavaScript -->
            </tbody>
        </table>
    </div>
</div>

<script>
    async function loadSignalements() {
        try {
            const response = await fetch('/api/signalements');
            const signalements = await response.json();
            const tableBody = document.getElementById('signalementsTableBody');
            tableBody.innerHTML = '';

            signalements.forEach(s => {
                const date = s.dateSignalement ? new Date(s.dateSignalement).toLocaleString() : 'N/A';
                const row = `
                    <tr>
                        <td>\${date}</td>
                        <td>\${s.utilisateur ? s.utilisateur.email : 'N/A'}</td>
                        <td>\${s.latitude}</td>
                        <td>\${s.longitude}</td>
                        <td>
                            <span class="badge \${getBadgeClass(s.statut ? s.statut.nom : '')}">
                                \${s.statut ? s.statut.nom : 'Inconnu'}
                            </span>
                        </td>
                        <td>
                            <a href="/signalements/modifier/\${s.id}" class="btn btn-sm btn-primary">Modifier</a>
                            <button onclick="deleteSignalement('\${s.id}')" class="btn btn-sm btn-danger">Supprimer</button>
                        </td>
                    </tr>
                `;
                tableBody.insertAdjacentHTML('beforeend', row);
            });
        } catch (error) {
            console.error('Erreur lors du chargement des signalements:', error);
        }
    }

    function getBadgeClass(statut) {
        switch(statut.toLowerCase()) {
            case 'nouveau': return 'bg-info';
            case 'en cours': return 'bg-warning text-dark';
            case 'terminé': return 'bg-success';
            default: return 'bg-secondary';
        }
    }

    async function deleteSignalement(id) {
        if (confirm('Êtes-vous sûr de vouloir supprimer ce signalement ?')) {
            try {
                const response = await fetch(`/api/signalements/\${id}`, {
                    method: 'DELETE'
                });
                if (response.ok) {
                    loadSignalements();
                } else {
                    alert('Erreur lors de la suppression');
                }
            } catch (error) {
                console.error('Erreur:', error);
            }
        }
    }

    document.addEventListener('DOMContentLoaded', loadSignalements);
</script>

</body>
</html>
