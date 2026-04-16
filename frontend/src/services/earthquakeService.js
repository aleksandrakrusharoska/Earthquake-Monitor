import axios from 'axios';

const BASE_URL = '/api/earthquakes';

const api = axios.create({
  baseURL: BASE_URL,
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
});

const earthquakeService = {
  getAll: () => api.get(''),
  getById: (id) => api.get(`/${id}`),
  fetchAndStore: () => api.post('/fetch'),
  filterByMagnitude: (min) => api.get(`/filter/magnitude?min=${min}`),
  filterByTime: (afterMs) => api.get(`/filter/time?after=${afterMs}`),
  deleteById: (id) => api.delete(`/${id}`)
};

export default earthquakeService;
