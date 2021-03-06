# genetic-sound

An ongoing experiment in synthetic audio reproduction using genetic algorithms and additive synthesis.

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
 - SQUARE
 - SAWTOOTH
 
# Discussion Points and Problems

- Blank vs merging canvas
    - Merging makes discovery difficult.  
- Mutation rates
    - Convergence vs divergence. Local/Global maximum. Stability
    - Trial: make mutation rate a function of generation number: the higher the generation number, the lower the mutation rate.
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
- ~~Fitness function calculates the absolute total sum different between the target and individual. Perhaps this should be 0.0 <= f <= 1.0 and only consider the range of the individual being added. Then weight this for the percentage of range that the individual occupies.~~
    - That idea won't work at an "Individual", because they are made up of many (possibly overlapping clips) and fitness is evaluated for Individuals, not Clips.
    - Could instead factor in the total length of clips for the individual against the total diff
- Use a "step size" for the Fitness function to improve performance
    - Accuracy cost?
    - Step size Nyguist frequency?
- Does individual fitness actually make sense when its fitness is dependent on those waveforms expressed before it?
    - Is the current actually only proposing a single solution, because each individual cannot be treated in isolation?
    - The problem is we are effectively only coming up with a single solution each generation.
    - The expressing of each individual should be done in isolation from each other - new target canvas each time.
    - Curerntly, wouldn't the last expressed Individual's fitness be the best indicator of population fitness?
    - Perhaps the population could instead be used to create independent collections of Individuals which are treated as solutions.
        - Could then have multiple solutions which could be independently evolved: each Individual self-contains an additive synthesis solution using the clips defined in its genes.

## Answered Questions

- Question: Why does the best fitness go backwards for a between generation?
    - Answer: Because it may have been affected by individuals which are expressed before them.

- Question: What are the trade-offs in having many individuals with only a few genes or only a few individuals with many genes?
    - Initial testing results indicate that in my case, more individuals with fewer genes gives better results.
        - I suspect this is because individuals are less resilient, therefore can be more easily removed from the population if they have a bad gene.
        - Stability for relatively high fitness individuals can be controlled through elitism.
        - High gene counts also make it difficult for the algorithm to optimize specific areas, such as silence.

## Trials TODO

- Trial enforcing minimum lengths for the clips?
- Trial generating clips as a single cycle/buffer and repeating, rather than calculating.
    - This could reduce the time to generate a population.
- Trial using a selector weighted by actual fitness, not by rank. Standout Individuals should be preferred.
    
# Helpful Info

## Genetic Algorithms

## Synthesizing Audio

- Frame rate is equivalent to sample rate, but includes the concept of a frame, rather than individual samples. 
    - A frame contains all the data required to express the audio at a given instance.
    - For mono, 16 bit audio as used the frame size is 2 bytes.

- A good introduction to digital audio synthesis: http://evanxmerz.com/soundsynthjava/Sound_Synth_Java.html
- http://www.cim.mcgill.ca/%7Eclark/nordmodularbook/nm_book_toc.html
- https://www.soundonsound.com/techniques/synth-secrets