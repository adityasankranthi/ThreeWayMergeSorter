### Project Name: **ThreeWayMergeSorter**

### GitHub README

# ThreeWayMergeSorter

## Overview
**ThreeWayMergeSorter** is a project designed to extend the functionality of a ranking class by implementing a three-way merge sort algorithm. This assignment involves updating the `Ranking` class from Homework #10 to include sorting capabilities using a novel three-way merge approach. The project focuses on stability in sorting and efficiency in recursive code.

## Features
- **Ranking ADT Update**: Adds sorting functionality to the existing `Ranking` class.
- **Three-Way Merge Sort**: Implements a merge sort algorithm using a three-way merge technique.
- **Stability**: Ensures that equivalent elements maintain their relative order after sorting.
- **Efficiency Testing**: Includes tests to ensure the sorting algorithm performs efficiently.

## Key Components
- **`Ranking` Class**: Updated to include sorting methods.
  - `toSortedArray(array, comparator)`: Sorts elements and places them into the provided or newly created array using a comparator.
  
- **Helper Methods**:
  - `isSorted(comp, array, lo, hi)`: Checks if a portion of the array is sorted according to the comparator.
  - `merge3(comp, lo1, lo2, hi3, out, in)`: Performs a three-way merge on sorted sequences from an input array into an output array.
  - `doMergesort(r, comp, start, out, tmp)`: Recursively sorts elements in a binary tree structure and merges them using the three-way merge.

## Usage
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/yourusername/ThreeWayMergeSorter.git
   ```
2. **Navigate to the Project Directory**:
   ```bash
   cd ThreeWayMergeSorter
   ```
3. **Open in IDE**: Open the project in your preferred Java IDE (e.g., Eclipse).

## Running Tests
- **JUnit Tests**: The repository includes various JUnit tests to verify the correctness and efficiency of the implementation.
  - **Tests for New Methods**: `src/TestRankingNew.java`
  - **Exhaustive Sorting Tests**: `src/TestSort.java`
  - **Efficiency Tests**: `src/TestEfficiencyNew.java`

## Implementation Steps
1. **Unlock Locked Tests**: Begin by unlocking the locked tests to understand the required arguments and expected behavior of the helper methods.
2. **Implement Helper Methods**: Develop the `isSorted`, `merge3`, and `doMergesort` methods.
3. **Update `Ranking` Class**: Implement the `toSortedArray` method using the helper methods.
4. **Run Tests**: Use `TestRankingNew` to check for obvious bugs. 
5. **Check Edge Cases**: Use `TestSort` to find and fix any remaining issues.
6. **Efficiency Testing**: Use `TestEfficiencyNew` to ensure that the algorithm performs efficiently.
7. **Refactor if Needed**: Make necessary changes based on efficiency testing results and revalidate with earlier tests.
