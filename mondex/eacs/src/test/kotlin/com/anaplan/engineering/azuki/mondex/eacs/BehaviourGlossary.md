The behaviours with the index AB are expressed in terms of the abstract model A. The behaviour with the index CON is
expressed in terms of the concrete model C.

Each behaviour (BEH) is taken from a security property as specified in chapter 2 of the Mondex spec.

- BEHAB1 **NoValueCreation** No value may be created in the system: the sum of all the purses balances does not increase
- BEHAB2 **AllValueAccounted** All value must be accounted for in the system: the sum of all purses balances and lost components does not change
- BEHAB3 **Authentic** A transfer can occur only between authentic purses
- BEHAB4 **SufficientFundsProperty** A transfer can only occur if there are sufficient funds in the from-purse
- BEHCON5 **LogIfNecessary** If a purse aborts a transfer at a point where value could be lost, then the purse logs the details
