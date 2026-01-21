<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <title>Nouveau Signalement</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        body { padding: 20px; background-color: #f8f9fa; }
        .form-container { max-width: 600px; margin: auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
    </style>
</head>
<body>

<div class="container">
    <div class="form-container">
        <h2 class="mb-4 text-center">Nouveau Signalement</h2>
        
        <form action="/signalements/nouveau" method="post">
            <div class="mb-3">
                <label for="email" class="form-label">Email Utilisateur</label>
                <input type="email" class="form-control" id="email" name="email" placeholder="exemple@mail.com" required>
            </div>

            <div class="row">
                <div class="col-md-6 mb-3">
                    <label for="latitude" class="form-label">Latitude</label>
                    <input type="number" step="any" class="form-control" id="latitude" name="latitude" placeholder="ex: -18.879" required>
                </div>
                <div class="col-md-6 mb-3">
                    <label for="longitude" class="form-label">Longitude</label>
                    <input type="number" step="any" class="form-control" id="longitude" name="longitude" placeholder="ex: 47.507" required>
                </div>
            </div>

            <div class="mb-3">
                <label for="description" class="form-label">Description</label>
                <textarea class="form-control" id="description" name="description" rows="3" placeholder="Décrivez le problème..." required></textarea>
            </div>

            <div class="row">
                <div class="col-md-6 mb-3">
                    <label for="surfaceM2" class="form-label">Surface (m²)</label>
                    <input type="number" step="any" class="form-control" id="surfaceM2" name="surfaceM2" placeholder="ex: 15.5">
                </div>
                <div class="col-md-6 mb-3">
                    <label for="budget" class="form-label">Budget estimé</label>
                    <input type="number" step="0.01" class="form-control" id="budget" name="budget" placeholder="ex: 5000.00">
                </div>
            </div>

            <div class="mb-3">
                <label for="entrepriseConcerne" class="form-label">Entreprise concernée</label>
                <input type="text" class="form-control" id="entrepriseConcerne" name="entrepriseConcerne" placeholder="Nom de l'entreprise">
            </div>

            <div class="mb-3">
                <label for="photoUrl" class="form-label">URL de la photo</label>
                <input type="text" class="form-control" id="photoUrl" name="photoUrl" placeholder="https://lien-image.com/photo.jpg">
            </div>

            <div class="d-grid gap-2 mt-4">
                <button type="submit" class="btn btn-success">Créer le signalement</button>
                <a href="/signalements" class="btn btn-outline-secondary">Annuler</a>
            </div>
        </form>
    </div>
</div>

</body>
</html>
