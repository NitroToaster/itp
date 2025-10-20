import axios from 'axios';

export const api = axios.create({
    baseURL: process.env.REACT_APP_API_URL || 'mock', // use 'mock' until backend ready
    headers: { 'Accept': 'application/json' }
});