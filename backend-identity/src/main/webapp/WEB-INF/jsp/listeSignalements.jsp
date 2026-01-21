<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>Liste des Signalements</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <style>
        body { padding: 20px; background-color: #f8f9fa; }
        .table-container { background: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
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
            <tbody>
                <c:forEach items="${signalements}" var="s">
                    <tr>
                        <td>${s.dateSignalement}</td>
                        <td>${s.utilisateur.email}</td>
                        <td>${s.latitude}</td>
                        <td>${s.longitude}</td>
                        <td>
                            <span class="badge ${s.statut.nom == 'nouveau' ? 'bg-info' : (s.statut.nom == 'en cours' ? 'bg-warning' : 'bg-success')}">
                                ${s.statut.nom}
                            </span>
                        </td>
                        <td>
                            <a href="/signalements/modifier/${s.id}" class="btn btn-sm btn-primary">Modifier</a>
                        </td>
                    </tr>
                </c:forEach>
            </tbody>
        </table>
    </div>
</div>

</body>
</html>
