package com.anaplan.engineering.vdmanimation.api

sealed class VdmAnimationException(e: Exception) : RuntimeException(e)

class VdmPreconditionFailure(e: Exception) : VdmAnimationException(e)

class VdmPostconditionFailure(e: Exception) : VdmAnimationException(e)

class VdmInvariantFailure(e: Exception) : VdmAnimationException(e)

class VdmDeclarationFailure(e: Exception) : VdmAnimationException(e)
