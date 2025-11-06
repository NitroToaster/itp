import React, { useEffect, useState, useCallback } from 'react';
import { fetchItems } from '../../services/api/FridgeService';

export default function RecipeDetailModal({ 
  recipe, 
  loading, 
  onClose, 
  onAddToShopping 
}) {
  const [missingIngredients, setMissingIngredients] = useState([]);
  const [loadingMissing, setLoadingMissing] = useState(false);

  const loadMissingIngredients = useCallback(async () => {
    if (!recipe || !recipe.ingredients) return;
    
    setLoadingMissing(true);
    try {
      // Get fridge items
      const fridgeItems = await fetchItems();
      
      // Create set of lowercase fridge item names (same logic as JavaFX)
      const fridgeItemNames = new Set(
        fridgeItems.map(item => item.name.toLowerCase().trim())
      );
      
      // Find ingredients NOT in fridge
      const missing = recipe.ingredients.filter(ing => {
        const name = ing.name?.trim() || '';
        if (!name) return false;
        return !fridgeItemNames.has(name.toLowerCase());
      });
      
      setMissingIngredients(missing);
    } catch (error) {
      console.error('Failed to load missing ingredients:', error);
      setMissingIngredients([]);
    } finally {
      setLoadingMissing(false);
    }
  }, [recipe]);

  useEffect(() => {
    if (recipe && !loading) {
      loadMissingIngredients();
    }
  }, [recipe, loading, loadMissingIngredients]);

  const checkIngredientInFridge = (ingredientName) => {
    return !missingIngredients.some(
      missing => missing.name.toLowerCase() === ingredientName.toLowerCase()
    );
  };

  const handleAddMissing = () => {
    if (missingIngredients.length === 0) {
      alert('You have all ingredients!');
      return;
    }
    
    missingIngredients.forEach(ingredient => {
      onAddToShopping({
        name: ingredient.name,
        qty: 1
      });
    });
    
    alert(`Added ${missingIngredients.length} missing ingredients to shopping list!`);
  };

  if (!recipe && !loading) return null;

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div 
        className="modal" 
        onClick={(e) => e.stopPropagation()}
        style={{ maxWidth: '600px', maxHeight: '80vh', overflow: 'auto' }}
      >
        {loading ? (
          <div style={{ padding: '40px', textAlign: 'center' }}>
            <p style={{ color: 'var(--text-secondary)' }}>Loading recipe details...</p>
          </div>
        ) : (
          <>
            {/* Header */}
            <div className="modal-header" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3 className="modal-title">{recipe.title}</h3>
              <button
                onClick={onClose}
                style={{
                  background: 'transparent',
                  border: 'none',
                  color: 'var(--text-secondary)',
                  fontSize: '24px',
                  cursor: 'pointer',
                  padding: '0',
                  width: '32px',
                  height: '32px'
                }}
              >
                ✕
              </button>
            </div>

            <div className="modal-body">
              {/* Image */}
              {recipe.image && (
                <div style={{
                  width: '100%',
                  height: '250px',
                  background: 'var(--bg-secondary)',
                  borderRadius: 'var(--radius)',
                  overflow: 'hidden',
                  marginBottom: '24px'
                }}>
                  <img
                    src={recipe.image}
                    alt={recipe.title}
                    style={{
                      width: '100%',
                      height: '100%',
                      objectFit: 'cover'
                    }}
                  />
                </div>
              )}

              {/* Ingredients */}
              <div style={{ marginBottom: '24px' }}>
                <h4 style={{
                  fontSize: '16px',
                  fontWeight: '600',
                  color: 'var(--text-primary)',
                  marginBottom: '12px'
                }}>
                  Ingredients:
                </h4>
                {loadingMissing ? (
                  <p style={{ color: 'var(--text-secondary)' }}>Checking ingredients...</p>
                ) : (
                  <ul style={{
                    listStyle: 'none',
                    padding: 0,
                    margin: 0
                  }}>
                    {recipe.ingredients?.map((ingredient, index) => {
                      const inFridge = checkIngredientInFridge(ingredient.name);
                      return (
                        <li
                          key={index}
                          style={{
                            padding: '8px 12px',
                            marginBottom: '4px',
                            background: 'var(--bg-tertiary)',
                            borderRadius: 'var(--radius)',
                            display: 'flex',
                            justifyContent: 'space-between',
                            alignItems: 'center'
                          }}
                        >
                          <span style={{ color: 'var(--text-primary)' }}>
                            {ingredient.name}
                          </span>
                          <span style={{
                            display: 'flex',
                            alignItems: 'center',
                            gap: '8px'
                          }}>
                            <span style={{ color: 'var(--text-secondary)', fontSize: '14px' }}>
                              {ingredient.measure}
                            </span>
                            <span style={{ fontSize: '18px' }}>
                              {inFridge ? '✓' : '✗'}
                            </span>
                          </span>
                        </li>
                      );
                    })}
                  </ul>
                )}
              </div>

              {/* Instructions */}
              {recipe.instructions && (
                <div style={{ marginBottom: '24px' }}>
                  <h4 style={{
                    fontSize: '16px',
                    fontWeight: '600',
                    color: 'var(--text-primary)',
                    marginBottom: '12px'
                  }}>
                    Instructions:
                  </h4>
                  <div style={{
                    color: 'var(--text-secondary)',
                    lineHeight: '1.6',
                    whiteSpace: 'pre-line'
                  }}>
                    {recipe.instructions}
                  </div>
                </div>
              )}

              {/* Missing Ingredients Info */}
              {!loadingMissing && missingIngredients.length > 0 && (
                <div style={{
                  padding: '12px',
                  background: 'rgba(251, 146, 60, 0.1)',
                  border: '1px solid rgba(251, 146, 60, 0.3)',
                  borderRadius: 'var(--radius)',
                  marginBottom: '16px'
                }}>
                  <p style={{
                    margin: 0,
                    fontSize: '14px',
                    color: 'var(--text-secondary)'
                  }}>
                    You're missing {missingIngredients.length} ingredient{missingIngredients.length !== 1 ? 's' : ''}
                  </p>
                </div>
              )}
            </div>

            {/* Footer */}
            <div className="modal-footer" style={{ display: 'flex', gap: '8px', justifyContent: 'flex-end' }}>
              <button className="btn" onClick={onClose}>
                Close
              </button>
              {onAddToShopping && missingIngredients.length > 0 && (
                <button 
                  className="btn primary" 
                  onClick={handleAddMissing}
                >
                  Add Missing to Shopping List
                </button>
              )}
            </div>
          </>
        )}
      </div>
    </div>
  );
}