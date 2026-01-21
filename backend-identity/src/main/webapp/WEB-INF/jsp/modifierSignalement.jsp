<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Modifier le Signalement</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        body { padding: 20px; background-color: #f8f9fa; }
        .form-container { max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
    </style>
</head>
<body>

<div class="container">
    <div class="form-container">
        <h2 class="mb-4 text-center">Modifier le Signalement</h2>
        
        <form action="/signalements/modifier" method="post">
            <input type="hidden" name="id" value="${signalement.id}">

            <div class="mb-3">
                <label class="form-label">ID Firebase</label>
                <input type="text" class="form-control" value="${signalement.idFirebase}" readonly>
            </div>

            <div class="mb-3">
                <label class="form-label">Email Utilisateur</label>
                <input type="text" class="form-control" value="${signalement.utilisateur.email}" readonly>
            </div>

            <div class="row">
                <div class="col-md-6 mb-3">
                    <label for="latitude" class="form-label">Latitude</label>
                    <input type="number" step="any" class="form-control" id="latitude" name="latitude" value="${signalement.latitude}" required>
                </div>
                <div class="col-md-6 mb-3">
                    <label for="longitude" class="form-label">Longitude</label>
                    <input type="number" step="any" class="form-control" id="longitude" name="longitude" value="${signalement.longitude}" required>
                </div>
            </div>

            <div class="mb-3">
                <label for="statutId" class="form-label">Statut</label>
                <select class="form-select" id="statutId" name="statutId" required>
                    <c:forEach items="${statuts}" var="s">
                        <option value="${s.id}" ${s.id == signalement.statut.id ? 'selected' : ''}>
                            ${s.nom}
                        </option>
                    </c:forEach>
                </select>
            </div>

            <hr>
            <h5 class="mb-3">Détails du Signalement</h5>

            <div class="mb-3">
                <label for="description" class="form-label">Description</label>
                <textarea class="form-control" id="description" name="description" rows="3" required>${details.description}</textarea>
            </div>

            <div class="row">
                <div class="col-md-6 mb-3">
                    <label for="surfaceM2" class="form-label">Surface (m²)</label>
                    <input type="number" step="any" class="form-control" id="surfaceM2" name="surfaceM2" value="${details.surfaceM2}">
                </div>
                <div class="col-md-6 mb-3">
                    <label for="budget" class="form-label">Budget estimé</label>
                    <input type="number" step="0.01" class="form-control" id="budget" name="budget" value="${details.budget}">
                </div>
            </div>

            <div class="mb-3">
                <label for="entrepriseConcerne" class="form-label">Entreprise concernée</label>
                <input type="text" class="form-control" id="entrepriseConcerne" name="entrepriseConcerne" value="${details.entrepriseConcerne}">
            </div>

            <div class="mb-3">
                <label for="photoUrl" class="form-label">URL de la photo</label>
                <input type="text" class="form-control" id="photoUrl" name="photoUrl" value="${details.photoUrl}">
            </div>

            <div class="d-grid gap-2 mt-4">
                <button type="submit" class="btn btn-primary">Enregistrer les modifications</button>
                <a href="/signalements" class="btn btn-outline-secondary">Annuler</a>
            </div>
        </form>
    </div>
</div>

</body>
</html>
