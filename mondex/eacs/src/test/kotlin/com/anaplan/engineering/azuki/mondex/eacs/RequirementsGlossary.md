Each requirement is taken from a security property as specified in chapter 2 of the Mondex spec.

- **NoValueCreation** No value may be created in the system: the sum of all the purses balances does not increase
- **AllValueAccounted** All value must be accounted for in the system: the sum of all purses balances and lost components does not change
- **Authentic** A transfer can occur only between authentic purses
- **SufficientFundsProperty** A transfer can only occur if there are sufficient funds in the from-purse
- **LogIfNecessary** If a purse aborts a transfer at a point where value could be lost, then the purse logs the details
