
/**
 * Parallel Pattern Library for KeYmeara models with timed automata or threads
 *
 * A collection of macros expanding to formalizations of hybrid timed
 * automata (or threads) executing in parallel to some physics
 * evolution. Each thread executes a combination of global and local
 * effects. In each step at most one global effect must happen. The
 * time between subsequent global effects is captured by the time
 * constraint over par_dt in each step. That is, if a thread T has two
 * global effects A and B and executes in between some program c with
 * local effect only, this thread would translate into:
 *
 *    step_seq(T, 1, A;c, |A| + |c|) ++ step_seq(T, 2, B, |B|)
 *
 * where |x| is the execution time of x and 1, 2 are identifiers for the steps.
 *
 * Note, parallel evolution of two physics a' = ... & H, b' = ... & I is trivial
 * by combination into a' = ..., b' = ... & (H & I)
 */



/* programVariables 
 *==================*/

/* defines program variables for parallel package */
#define parallel \
  R par_dt         /* time variable used for constraining step execution time */

/* defines program variables for a thread A 
 * - A name of thread
 */
#define par_thread(A) \ 
  R par_step_##A;  /* next step of A (0 = stop) */ \
  R par_timer_##A  /* time until A can make its next step */


/* problem 
 *=========*/

/* initialize thread 
 * - A: name of thread
 * - G: identifier of initial state (!= 0)
 * - start_time: condition over the variable par_dt denoting start time
 */
#define par_init(A, G, start_time) \
  par_step_##A = G & (\exists R par_dt. (par_timer_##A = par_dt & start_time))


/* next program step
 *-------------------*/

/* set next step
 * - A: name of thread
 * - G: identifier of the next state where to continue (!= 0)
 */
#define par_next(A, G) \
  par_step_##A := G

/* stop thread
 * - A: name of thread to stop by setting the identifier to 0
 */
#define par_stop(A) \
  par_next(A, 0)

/* hybrid program step 
 *---------------------
 * if eligible (par_step_A = G & !(par_timer_A > 0)) advance A by executing H for time
 * H should call next_step(A, G) with G being the guard of the next step to execute
 *
 * - A: name of thread
 * - G: identifier of this state
 * - time: condition over par_dt denoting the time until the next step with global effect.
 * - H: hybrid program to execute
 */
#define par_do_step(A, G, time, H) \
  (? ( (par_timer_##A = 0) & (par_step_##A != 0) ); par_dt := *; ? (time); par_timer_##A := par_dt; H)

/* shortcut for par_do_step(A, G, time, H; par_next(A, G+1))
 */
#define par_seq(A, G, time, H) \
  (? ( (par_timer_##A = 0) & (par_step_##A != 0) ); par_dt := *; ? (time); par_timer_##A := par_dt; H; par_next(A, G+1))

/* true iff thread A is active (i.e., next state identifier != 0) */
#define par_active(A) \
  par_step_##A != 0

/* true iff some time remains until the next step of thread A with global effect */
#define par_waiting(A) \
  par_active(A) -> par_timer_##A > 0

/* advance time of active thread A */
#define par_advance(A) \
  par_timer_##A > 0 -> par_timer_##A' = -1


/**
 * Usage:

  while (par_active(A) | par_active(B) | ...)         
    program_A++                                 
    program_B++        
    ...                         
    { par_advance(A),                           
      par_advance(B),                         
      ...  
      C                                         
    &                                           
      (H & par_waiting(A) & par_waiting(B) & ...)}    
  end
    
*/
