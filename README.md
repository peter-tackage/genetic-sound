# genetic-sound

An ongoing experiment in synthetic audio reproduction using genetic algorithms.

The synthesis is primarily aimed at reproducing the sounds of musical instruments.

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
    - Assess the individual's fitness once as it is rendered.
1. Write the newly evolved audio canvas to an output audio file. 
1. Create the next population generation:
    - Sort the population by fitness.
    - Using Elitism; select which individuals should be directly retained into the new population.
    - Replace the remaining individuals by selecting pairs for reproduction from the entire population.
    - Create replacement individuals by combining the genes of the pairs, according to the Crossover and Mutation strategies.  
1. Repeat forever.

# Genetic Operators

This is a short description of the current state of the generic operators used in the algorithm and their strategies.

## Fitness

Fitness is evaluate as the sum of the differences between the samples of the individual rendered on the audio canvas and the original source audio.

Thus, a lower fitness value is better. 

 - AmplitudeDiffFitnessFunction
 - CoroutineAmplitudeDiffFitnessFunction

## Selection

A Selector returns a single individual from the population to define a parent for the next generation. All selection is currently performed on a fitness sorted population. 

- RankSelector
- CutoffSelector

### Elitism

An individual is currently "elite" if they have a better than average fitness. 

## Crossover

- UniformZipperCrossOver

## Mutation

### MutationProbability

Determines the likelihood of mutation of a gene.

- ConstantMutationProbability
- VarianceMutationProbability
    - Is this disruptive?
- TODO GenerationMutationProbability
    - Make the mutation probability a function of the generation number.
    
### Mutator

Performs the mutation of a gene.

Currently only mutates sinusoids by changing either:
- Amplitude
- Start position
- End position
- Frequency

## Clips

### Waveforms

 - SINUSOID
 - TRIANGLE (TODO)
 - SQUARE (TODO)
 - NOISE (TODO)
 - DC (TODO)
 
### Amplitude Modulation

 - SWELL (TODO)
 - DECAY (TODO)
 - SINUSOID (TODO)
 - NONE 
 
# Discussion Points and Problems

- Blank vs merging canvas
    - Merging makes discovery difficult.  
- Population vs gene size
    - What are the tradeoffs in having many individuals with only a few genes or only a few individuals with many genes?
- Mutation rates
    - Convergence vs divergence. Local/Global maximum. Stability
- Selection
- Elitism
    - Is better than average actually elite - should the elite group be smaller?
- Rendering performance
- Clip complexity
    - More complex waveforms made improve performance vs increasing the sinusoidal population size. 
- Rendering techniques
- Fitness function depending on population ordering 
    - The fitness of a given individual cannot be considered in isolation. Changes to individuals, will impact subsequent individuals.    
- Should selection not choose from elite individuals?
    - Currently elite individuals are included, which may be negatively impacting discovery by reducing diversity
- Improving Generational Rate
    - How to make the rendering/expressing faster? Currently 30ms for 100 genes.
- Termination strategies
    - Generation, time/resources, fitness, manual inspection
- Out of phase signals reducing the amplitude.
- The affect of clip order in the signal merging result.
    - When merging a clip the resultant merged signal is the average of the existing audio canvas and the clip to be merged. This means that clips added later will be stronger in the mix. This usually means that the less fit signals are unfortunately proportionally stronger than the fitter signals.
- Use of spectral analysis to calculate better fitness results.
- Effective use of Kotlin coroutines.
    - Reward vs overhead - where should they be used?
- Fitness function calculates the absolute total sum different between the target and individual. Perhaps this should be 0.0 <= f <= 1.0 and only consider the range of the individual being added. Then weight this for the percentage of range that the individual occupies
- Use a "step size" for the Fitness function to improve performance
    - Accuracy cost?