export interface StatutSignalement {
  id: number;
  nom: string;
}

export interface Signalement {
  id?: string;
  postgresId?: string;
  idFirebase?: string;
  dateSignalement: string;
  statut: string; // Changé de StatutSignalement à string pour correspondre au DTO
  latitude: number;
  longitude: number;
  description?: string;
  surfaceM2?: number;
  budget?: any;
  entrepriseConcerne?: string;
  photoUrl?: string;
  utilisateur?: {
    email: string;
  };
}
