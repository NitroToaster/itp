## User Story 1: Expiration Date Sorted Item List

### User story
As a fridge owner, I want to see a list of all items sorted by expirationdate so that I can quickly find what kind of products I have available and when they expire.

### Business Context
Food waste is a significant concern for households, with expired products often going unnoticed until it's too late. By providing a clear, date-sorted view, users can proactively use items before they expire, reducing waste and saving money. This feature serves as the primary inventory management tool for the application.

### Acceptance Criteria

- Given I have items in my fridge When I view the item list Then all items are displayed sorted by expiration date (earliest first)
- Given multiple items have the same expiration date When viewing the list Then items with the same date are sub-sorted alphabetically by name

### User Persona
Name: Sarah, Busy Professional

Age: 32

- Context: Works long hours, shops weekly, often forgets what's in her fridge
- Pain Point: Frequently discovers expired food when cleaning the fridge, feels guilty about waste
- Goal: Minimize food waste and make quick decisions about meal planning



## User Story 2: Quick Item Quantity Access

### User story: 
As a fridge owner, I would like to quickly access information about how much I have of a certain item so that I know what I should buy.

### Business Context
Users frequently need to check inventory while shopping or meal planning. Quick access to quantity information prevents over-purchasing and ensures users buy what they actually need. This feature supports the shopping list functionality and reduces the friction of inventory checking.

### Acceptance Criteria

- Given I have items in my fridge When I search for an item by name Then I see the item with its current quantity displayed prominently
- Given I am viewing the main list When looking at any item Then quantity is visible without requiring additional clicks/taps

### User Persona
Name: Marcus, Family Shopper

Age: 45

- Context: Shops for family of 4, often at grocery store wondering what's at home
- Pain Point: Calls family members to ask what's needed, or buys duplicates by accident
- Goal: Make informed purchasing decisions without wasting money on duplicates