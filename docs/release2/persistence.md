!!! Denne trenger utbedring/oppdatering !!! 

# Persistence Module

The persistence module manages reading from and writing to files for the fridge inventory system. Its purpose is to save the fridge’s contents between program runs in a simple, human-readable format. Jackson library is used to serialize and deserialize data between java objects and json format.

## Functionality

### Saving data:
- The method saveFridgeData(Fridge fridge, String filename) writes all items in the fridge to a text file. Each line contains: name,quantity,expirationDate
- Data is written using a PrintWriter, and the file is safely closed after writing

### Reading data:
- The method readFridgeData(Fridge fridge, String filename) loads items from the file using a Scanner
- Each line is split by commas, parsed into an Item object, and added to the fridge. Invalid or missing files are handled with try-catch blocks

## File format example

``` milk,2,2024-06-30 ```

``` eggs,12,2024-07-05 ```

```broccoli,1,2024-06-28 ```