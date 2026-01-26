import { Component, OnInit, AfterViewInit, inject, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { FormsModule } from '@angular/forms';
import * as L from 'leaflet';
import { SignalementService } from '../../services/signalement.service';
import { Signalement } from '../../models/signalement.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [CommonModule, RouterModule, FormsModule],
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.css']
})
export class MapComponent implements OnInit, AfterViewInit {
  private map: any;
  private signalements: Signalement[] = [];
  private signalementService = inject(SignalementService);
  public authService = inject(AuthService);

  // Form state removed - web is read-only
  
  @HostListener('window:resize', ['$event'])
  onResize() {
    if (this.map) {
      this.map.invalidateSize();
    }
  }

  ngOnInit(): void {
    this.loadSignalements();
  }

  ngAfterViewInit(): void {
    this.initMap();
    // Force Leaflet to recalculate container size after a small delay
    setTimeout(() => {
      if (this.map) {
        this.map.invalidateSize();
      }
    }, 500);
  }

  private initMap(): void {
    const tana = { lat: -18.8792, lng: 47.5079 };

    this.map = L.map('map').setView([tana.lat, tana.lng], 13);

    L.tileLayer('http://localhost:8082/styles/basic-preview/{z}/{x}/{y}.png', {
      maxZoom: 20,
      attribution: '© OpenStreetMap contributors'
    }).addTo(this.map);

    const iconDefault = L.icon({
      iconRetinaUrl: 'assets/leaflet/marker-icon-2x.png',
      iconUrl: 'assets/leaflet/marker-icon.png',
      shadowUrl: 'assets/leaflet/marker-shadow.png',
      iconSize: [25, 41],
      iconAnchor: [12, 41],
      popupAnchor: [1, -34],
      tooltipAnchor: [16, -28],
      shadowSize: [41, 41]
    });
    L.Marker.prototype.options.icon = iconDefault;
  }

  private loadSignalements(): void {
    this.signalementService.getAllSignalements().subscribe({
      next: (data) => {
        this.signalements = data;
        this.addMarkers();
      },
      error: (err) => console.error(err)
    });
  }

  private addMarkers(): void {
    if (!this.map) return;

    // Clear existing markers if any (optional, but good for refresh)
    this.map.eachLayer((layer: any) => {
      if (layer instanceof L.Marker) {
        this.map.removeLayer(layer);
      }
    });

    this.signalements.forEach(s => {
      const statusName = String(s.statut || 'Nouveau');
      const color = this.getStatusColor(s.statut);
      
      const marker = L.marker([s.latitude, s.longitude])
        .addTo(this.map)
        .bindPopup(`
          <div class="p-2">
            <div class="flex items-center mb-2">
              <span class="w-3 h-3 rounded-full mr-2" style="background-color: ${color}"></span>
              <strong class="text-slate-800">${statusName}</strong>
            </div>
            <p class="text-sm text-slate-600 mb-1">${s.description || 'Pas de description'}</p>
            <div class="text-[10px] text-slate-400 border-t pt-1">
              ${new Date(s.dateSignalement).toLocaleString()}
            </div>
          </div>
        `);
    });

    // Final refresh to ensure everything is visible
    this.map.invalidateSize();
  }

  private getStatusColor(status: string): string {
    const st = String(status || '').toLowerCase();
    switch (st) {
      case 'nouveau': return '#3b82f6'; // blue-500
      case 'en cours':
      case 'en_cours': return '#f59e0b'; // amber-500
      case 'terminé':
      case 'termine': return '#10b981'; // emerald-500
      default: return '#64748b'; // slate-500
    }
  }
}
