# genetic-sound

An ongoing experiment in synthetic audio reproduction using genetic algorithms.

# Configuration

At the top level the algorithm configured with the following components:

  **populationCount** - the total number of individuals in the population.
  
  **geneCount** - the number of genes (clips) held by each individual.
  
  **supportedClipTypes** - a list of the supported clip types (waveform types).
  
  **mutationProbability** - an function which provides the current gene mutation probability.
  
  **fitnessFunction** - a function which evaluates the fitness of an individual.
  
  **selector** - a function which determines which individuals are used to create the next population.
  
  **mutator** - a function which performs the mutation of a single gene.
  
  **crossOver** - a function which combines the genes of two individuals.
  
Depending on the function implementation, additional configuration is possible.

# Audio

For the sake of simplicity, the program only handles mono, 16-bit WAV file input files. WAV is Linear PCM encoded, which makes
population amplitude generation easier. There's no enforced limitation on input data endian or sample rate.

Audio is written out as mono, 16-bit WAV big-endian files at the same sample rate as the input file.

# Description

The population is comprised of individuals with a number of clips (waveforms) as their genes.

1. Randomly generate an initial population.
1. Render each individual in the population on a blank audio canvas:
    - Assessing the individual's fitness once it is rendered.
1. Write the newly evolved audio canvas to an output audio file. 
1. Create the next population generation:
    - Sort the population by fitness.
    - Using Elitism; select which individuals should be directly retained into the new population.
    - Replace the remaining individuals by selecting pairs for reproduction from the entire population.
    - Create replacement individuals by combining the genes of the pairs, according to the Crossover and Mutation strategies.  
1. Repeat forever.

# Genetic Operators

## Selection

### Elitism

## Crossover

## Mutation

# Discussion Points

- Blank vs merging canvas
    - Merging makes discovery difficult.  
- Population vs gene size
- Mutation rates
    - Convergence vs divergence. Local/Global maximum. Stability
- Selection
- Elitism
- Rendering performance
- Clip complexity
    - More complex waveforms made improve performance vs increasing the sinusoidal population size. 
- Rendering techniques
- Fitness function depending on population ordering    
- Should selection not choose from elite individuals?
    - Currently elite individuals are included, which may be negatively impacting discovery.
- Improving Generational Rate
- Termination strategies
    - Generation, time/resources, fitness, manual inspection

