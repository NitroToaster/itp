import { api } from './api';

// Fetch recipes with pagination - using filter2 endpoint for better results
export async function fetchRecipes(page = 1, limit = 12) {
  try {
    // Use search with a common letter to get diverse results
    const res = await api.get('/recipes/search', { params: { q: 'a' } });
    
    // Client-side pagination
    const start = (page - 1) * limit;
    const end = start + limit;
    const recipes = Array.isArray(res.data) ? res.data : [];
    const paginated = recipes.slice(start, end);
    
    return {
      recipes: paginated,
      hasMore: end < recipes.length,
      total: recipes.length
    };
  } catch (error) {
    console.error('fetchRecipes error:', error);
    return { recipes: [], hasMore: false, total: 0 };
  }
}

// Fetch single recipe details
export async function fetchRecipeById(id) {
  try {
    const res = await api.get(`/recipes/${id}`);
    return res.data;
  } catch (error) {
    console.error('fetchRecipeById error:', error);
    throw error;
  }
}

// Search recipes by name
export async function searchRecipes(query) {
  try {
    const res = await api.get('/recipes/search', { params: { q: query } });
    return res.data;
  } catch (error) {
    console.error('searchRecipes error:', error);
    return [];
  }
}

// Filter by category - using filter2 endpoint
export async function fetchRecipesByCategory(category, page = 1, limit = 12) {
  try {
    const offset = (page - 1) * limit;
    const res = await api.get('/recipes/filter2', { 
      params: { 
        category: category,
        limit: limit,
        offset: offset
      } 
    });
    
    const recipes = Array.isArray(res.data) ? res.data : [];
    
    return {
      recipes: recipes,
      hasMore: recipes.length === limit, // If we got a full page, there might be more
      total: recipes.length + offset // Approximate total
    };
  } catch (error) {
    console.error('fetchRecipesByCategory error:', error);
    return { recipes: [], hasMore: false, total: 0 };
  }
}

// Get missing ingredients using backend matching logic
export async function getMissingIngredients(recipeId) {
  try {
    const res = await api.post(`/recipes/${recipeId}/missing-ingredients`);
    return res.data;
  } catch (error) {
    console.error('getMissingIngredients error:', error);
    return [];
  }
}

// Favorites management (localStorage for now)
const FAVORITES_KEY = 'recipe_favorites_v1';

export function getFavorites() {
  try {
    const raw = localStorage.getItem(FAVORITES_KEY);
    return raw ? JSON.parse(raw) : [];
  } catch {
    return [];
  }
}

export function isFavorite(recipeId) {
  const favorites = getFavorites();
  return favorites.includes(recipeId);
}

export function toggleFavorite(recipeId) {
  try {
    let favorites = getFavorites();
    
    if (favorites.includes(recipeId)) {
      favorites = favorites.filter(id => id !== recipeId);
    } else {
      favorites.push(recipeId);
    }
    
    localStorage.setItem(FAVORITES_KEY, JSON.stringify(favorites));
    return { isFavorite: favorites.includes(recipeId) };
  } catch (error) {
    console.error('toggleFavorite error:', error);
    throw error;
  }
}

// Get available categories and areas
export async function fetchRecipeMeta() {
  try {
    const res = await api.get('/recipes/meta');
    return res.data; // Returns { categories: [...], areas: [...] }
  } catch (error) {
    console.error('fetchRecipeMeta error:', error);
    return { categories: [], areas: [] };
  }
}
