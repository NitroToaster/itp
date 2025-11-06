import React, { useState, useEffect } from 'react';
import {
  fetchRecipes,
  fetchRecipesByCategory,
  searchRecipes,
  fetchRecipeById,
  toggleFavorite,
  isFavorite,
  getFavorites,
  fetchRecipeMeta
} from '../../services/api/RecipeService';
import RecipeCard from '../../components/RecipeCard';
import RecipeDetailModal from '../../components/modals/RecipeDetailModal';

export default function RecipesPage({ onAddToShopping }) {
  const [recipes, setRecipes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(1);
  const [hasMore, setHasMore] = useState(true);
  
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('all');
  const [showFavoritesOnly, setShowFavoritesOnly] = useState(false);
  
  const [detailModalOpen, setDetailModalOpen] = useState(false);
  const [selectedRecipe, setSelectedRecipe] = useState(null);
  const [detailLoading, setDetailLoading] = useState(false);

  // Add state for categories
  const [categories, setCategories] = useState([]);
  const [categoriesLoading, setCategoriesLoading] = useState(true);

  // Load categories on mount
  useEffect(() => {
    loadCategories();
  }, []);

  const loadCategories = async () => {
    setCategoriesLoading(true);
    try {
      const meta = await fetchRecipeMeta();
      setCategories(meta.categories || []);
    } catch (error) {
      console.error('Failed to load categories:', error);
      setCategories([]);
    } finally {
      setCategoriesLoading(false);
    }
  };

  useEffect(() => {
    loadRecipes();
  }, []);

  const loadRecipes = async () => {
    setLoading(true);
    try {
      const data = await fetchRecipes(1, 12);
      setRecipes(data.recipes || []);
      setHasMore(data.hasMore || false);
      setPage(1);
    } catch (error) {
      console.error('Failed to load recipes:', error);
    } finally {
      setLoading(false);
    }
  };

  const loadMore = async () => {
    if (loading || !hasMore) return;
    
    setLoading(true);
    try {
      const nextPage = page + 1;
      let data;
      
      if (selectedCategory !== 'all') {
        data = await fetchRecipesByCategory(selectedCategory, nextPage, 12);
      } else {
        data = await fetchRecipes(nextPage, 12);
      }
      
      setRecipes(prev => [...prev, ...(data.recipes || [])]);
      setHasMore(data.hasMore || false);
      setPage(nextPage);
    } catch (error) {
      console.error('Failed to load more recipes:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleSearch = async (query) => {
    setSearchQuery(query);
    
    if (!query.trim()) {
      loadRecipes();
      return;
    }
    
    setLoading(true);
    try {
      const results = await searchRecipes(query);
      setRecipes(results);
      setHasMore(false);
      setPage(1);
    } catch (error) {
      console.error('Search failed:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleCategoryChange = async (category) => {
  setSelectedCategory(category);
  setSearchQuery('');
  setShowFavoritesOnly(false);
  
  // If "all" is selected, just load recipes normally
  if (category === 'all') {
    loadRecipes();
    return;
  }
  
  setLoading(true);
  try {
    const data = await fetchRecipesByCategory(category, 1, 12);
    setRecipes(data.recipes || []);
    setHasMore(data.hasMore || false);
    setPage(1);
  } catch (error) {
    console.error('Failed to filter by category:', error);
  } finally {
    setLoading(false);
  }
};

//for handling favorites tab
  const loadFavorites = async () => {
    setLoading(true);
    try {
      const favoriteIds = getFavorites();
      const favoriteRecipes = [];

      for (const id of favoriteIds) {
        try {
          const recipe = await fetchRecipeById(id);
          if (recipe) {
            favoriteRecipes.push(recipe);
          }
        } catch (e) {
          console.error(`Failed to load recipe ${id}:`, e);
        }
      }

      setRecipes(favoriteRecipes);
      setHasMore(false);
      setPage(1);
    } catch (error) {
      console.error('Failed to load favorites:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleShowFavorites = async () => {
    if (showFavoritesOnly) {
      setShowFavoritesOnly(false);
      setSelectedCategory('all');
      loadRecipes();
    } else {
      setShowFavoritesOnly(true);
      setSelectedCategory('favorites');
      setSearchQuery('');
      await loadFavorites();
    }
  };

  const handleRecipeClick = async (recipe) => {
    setSelectedRecipe(recipe);
    setDetailModalOpen(true);
    setDetailLoading(true);
    
    try {
      const detail = await fetchRecipeById(recipe.id);
      setSelectedRecipe(detail);
    } catch (error) {
      console.error('Failed to load recipe details:', error);
      alert('Failed to load recipe details');
    } finally {
      setDetailLoading(false);
    }
  };

 const handleToggleFavorite = async (recipeId, event) => {
  event.stopPropagation();
  
  try {
    toggleFavorite(recipeId);
    
    if (showFavoritesOnly) {
      await loadFavorites();
    } else {
      setRecipes([...recipes]);
    }
  } catch (error) {
    console.error('Failed to toggle favorite:', error);
  }
};

  return (
    <div className="panel">
      {/* Header */}
      <div className="page-header">
        <h2 className="page-title">Recipes</h2>
      </div>

      {/* Search and Filters */}
      <div style={{ marginBottom: '24px' }}>
        <div style={{ display: 'flex', gap: '12px', marginBottom: '16px' }}>
          <input
            className="search-input"
            placeholder="Search recipes..."
            value={searchQuery}
            onChange={(e) => handleSearch(e.target.value)}
            style={{ flex: 1 }}
          />
          <button
            className="btn"
            onClick={() => handleSearch('')}
          >
            Clear
          </button>
        </div>

        <div style={{ display: 'flex', gap: '12px', flexWrap: 'wrap' }}>
          <button
            className={`btn ${selectedCategory === 'all' && !showFavoritesOnly ? 'primary' : ''}`}
            onClick={() => handleCategoryChange('all')}
          >
            Mixed
          </button>
          
          {/* Dynamic category buttons */}
          {categoriesLoading ? (
            <span style={{ color: 'var(--text-secondary)' }}>Loading categories...</span>
          ) : (
            categories.slice(0, 5).map(category => (
              <button
                key={category}
                className={`btn ${selectedCategory === category ? 'primary' : ''}`}
                onClick={() => handleCategoryChange(category)}
              >
                {category}
              </button>
            ))
          )}
          
          <button
            className={`btn ${showFavoritesOnly ? 'primary' : ''}`}
            onClick={handleShowFavorites}
          >
            ❤️ Favorites
          </button>
        </div>
      </div>

      {/* Recipe Grid */}
      <div className="section">
        {loading && recipes.length === 0 ? (
          <div className="empty">Loading recipes...</div>
        ) : recipes.length === 0 ? (
          <div className="empty">No recipes found</div>
        ) : (
          <>
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(250px, 1fr))',
              gap: '20px',
              marginBottom: '24px'
            }}>
              {recipes.map(recipe => (
                <RecipeCard
                  key={recipe.id}
                  recipe={recipe}
                  isFavorite={isFavorite(recipe.id)}
                  onClick={() => handleRecipeClick(recipe)}
                  onToggleFavorite={(e) => handleToggleFavorite(recipe.id, e)}
                />
              ))}
            </div>

            {hasMore && !showFavoritesOnly && (
              <div style={{ textAlign: 'center' }}>
                <button
                  className="btn primary"
                  onClick={loadMore}
                  disabled={loading}
                >
                  {loading ? 'Loading...' : 'Load More Recipes'}
                </button>
              </div>
            )}
          </>
        )}
      </div>

      {/* Recipe Detail Modal */}
      {detailModalOpen && (
        <RecipeDetailModal
          recipe={selectedRecipe}
          loading={detailLoading}
          onClose={() => {
            setDetailModalOpen(false);
            setSelectedRecipe(null);
          }}
          onAddToShopping={onAddToShopping}
        />
      )}
    </div>
  );
}
