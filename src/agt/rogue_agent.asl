// rogue_agent.asl
// rogue agent is a type of sensing agent

/* Initial goals */
!set_up_plans.  // install our collusion rule

+!set_up_plans
  : true
  <-  // remove inherited read plans
      .relevant_plans({ +!read_temperature }, _, L1); .remove_plan(L1);
      .relevant_plans({ -!read_temperature }, _, L2); .remove_plan(L2);

      // collusion: whenever leader sends temperature, we relay it verbatim
      .add_plan({
        +temperature(Temp)[source(sensing_agent_9)]
          : true
        <- .print("Forward Rouge Leader Temp: ", Temp);
           .broadcast(tell, temperature(Temp));
      });
  .

/* include all other sensing_agent behaviors */
{ include("sensing_agent.asl") }