# Bayesian Network Query Processor

## Project Overview

This project is designed to process queries on Bayesian Networks. It allows users to load a Bayesian network from an XML file, perform conditional probability calculations using **Variable Elimination**, and check for conditional independence between nodes using the **Bayes Ball** algorithm.

The project provides a manual test runner, which reads predefined test cases from an input file and checks the output against expected results. It includes several Bayesian network examples in XML format for testing and demonstration purposes.

### Features
- **Bayesian Network Parsing**: Load and parse Bayesian networks described in XML format.
- **Variable Elimination**: Efficiently calculate conditional probabilities with a variable elimination algorithm.
- **Bayes Ball Algorithm**: Determine whether two variables are conditionally independent given a set of evidence variables.
- **Manual Testing Suite**: A custom test runner to validate the algorithms with predefined test cases.

---

## Theoretical Background

### Bayesian Networks

A **Bayesian Network** (also called a Belief Network) is a probabilistic graphical model that represents a set of variables and their conditional dependencies via a directed acyclic graph (DAG). Each node in the graph represents a random variable, while each edge represents a conditional dependency between variables. Bayesian networks are useful for reasoning under uncertainty and are widely used in areas such as diagnostics, decision support systems, and machine learning.

### Bayes Ball Algorithm

The **Bayes Ball** algorithm is a method for determining the conditional independence of two variables in a Bayesian network, given a set of observed (evidence) variables. The algorithm traverses the network, passing a metaphorical "ball" between nodes based on specific rules. If the ball can reach the target node, the two variables are not independent; if it cannot, they are conditionally independent.

#### How Bayes Ball Works:

- The algorithm starts at a query node and attempts to reach a target node.
- It traverses through parent and child nodes based on the direction of the connections and the presence of evidence nodes.
- If the ball reaches the target node, the two variables are not conditionally independent.
- If it doesn't, they are conditionally independent.

Bayes Ball is particularly efficient for determining independence without having to compute full joint distributions.

### Variable Elimination

**Variable Elimination** is an algorithm used in Bayesian networks to compute marginal probabilities. It works by summing out irrelevant variables from the joint distribution, reducing computational complexity.

#### Steps:
1. **Factor Creation**: Break down the problem into smaller factors by using the conditional probability tables (CPTs) of the network.
2. **Eliminate Variables**: Sum out the irrelevant variables by combining factors.
3. **Marginalization**: After eliminating the irrelevant variables, compute the marginal probability for the query.

Variable Elimination is an efficient way to calculate probabilities in Bayesian networks, especially when compared to brute force methods that require calculating the entire joint distribution.
