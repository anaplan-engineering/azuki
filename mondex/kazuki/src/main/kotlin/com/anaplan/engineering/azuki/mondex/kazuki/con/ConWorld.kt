package com.anaplan.engineering.azuki.mondex.kazuki.con

interface ConWorld {
    val conPurse: ConPurse // World-kind of module
    //.... other variables
}

interface AuxWorld : ConWorld {
    // various bits as invariants
}

/*
 S = [ x : nat | x < 5 ]

Op1 = [ Delta S; i?: int | x' = x + i? ]

pre Op1
=
forall S, i?: int & exists S' & Op1
= [def exp]
forall x: nat, i?: int | x < 5 & exists x': nat & S and S' and i? in int and x' = x + i?
= [def exp]
forall x: nat, i?: int | x < 5 & exists x': nat &
    x in nat x < 5 and x' in nat and x' < 5 and i? in int and x' = x + i?
= [forall-intro]
x in nat and i? in int and x < 5
=>
exists x': nat &
    x in nat x < 5 and x' in nat and x' < 5 and i? in int and x' = x + i?
= [logic]
x in nat and i? in int and x < 5
=>
x in nat x < 5 and  i? in int
and
exists x': nat &
     x' in nat and x' < 5 and x' = x + i?
= [imp-into, and-intro, refl]
x in nat and i? in int and x < 5
=>
exists x': nat &
     x' in nat and x' < 5 and x' = x + i?
= [ex-I]
x in nat and i? in int and x < 5
=>
(x' in nat and x' < 5)[x+i? / x']
= [subst]
x in nat and i? in int and x < 5 => (x+i?) in nat and (x+i?) < 5
= [logic]
x >= 0 and i? in int and x < 5 => x + i? >= 0 and x+i? < 5
= [arith]
x >= 0 and i? in int and x < 5 => i? >= 0 and x < 5 - i?

 @Module
interface S {
  val x: nat
  @Invariant
  fun isValid(): Boolean = x < 5

  function provides....

  val Op1 = functiomn() {
    command = { i, x -> x + i} ,
    pre = { i, x ->
        // x < 5 already from invariant
        i >= 0 and x < 5 - i?
    }
    post = { i, x, x' ->
        x' = x + i
    }
  }

 */
