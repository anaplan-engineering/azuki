# Track and Head-On Angle: Proposed Specification Fix

**Status:** Draft / design note  
**Scope:** VDM (`RightOfWayRules.vdmsl`), Kazuki (`RightOfWay.kt`), SampleImpl (`AirspaceV1.kt`)  
**Related predicates:** `track`, `headon`, `conv_not_headon`, `right_of_way`, safety theorems  
**Does not affect:** `QC`, `converging` (quadrant + HMD), `defaultPairConvergence`

---

## Summary

The current implementation defines aircraft track as:

```latex
track(a) = \tan^{-1}\!\left(\frac{v_x}{v_y}\right)
```

implemented as `atan(vx / vy)`. Two problems follow:

1. **Units mismatch:** `atan` returns **radians**, but `headon` / `conv_not_headon` compare against **degree-scale** thresholds (`180`, `Theta_h`).
2. **Direction loss:** `atan(vx/vy)` cannot distinguish opposite headings; head-on detection via `abs(track(a0) - track(a1))` is unreliable.

This document proposes corrected definitions and implementation options.

---

## Current Behaviour

### Definition (VDM / Kazuki)

```vdm
track(mk_Aircraft(-, mk_RVector(v0x, v0y))) == atan(v0x / v0y)
```

```kotlin
track(a) = atan(a.velocity.x / a.velocity.y)
```

### Head-on predicates (VDM / Kazuki)

```vdm
conv_not_headon(a0, a1)(delta_c, Theta_h) ==
    converging(a0, a1)(delta_c)
    and
    ( abs(track(a0) - track(a1)) < 180 - Theta_h
      or abs(track(a0) - track(a1)) > 180 + Theta_h )

headon(a0, a1)(delta_c, Theta_h) ==
    converging(a0, a1)(delta_c)
    and
    ( 180 - Theta_h <= abs(track(a0) - track(a1))
      and abs(track(a0) - track(a1)) < 180 + Theta_h )
```

With default `Theta_h = 80`:

| Predicate | Effective condition (today) | Typical `track_delta` (radians) |
|---|---|---|
| `conv_not_headon` | `track_delta < 100` OR `track_delta > 260` | max ≈ π ≈ 3.14 |
| `headon` | `100 <= track_delta <= 260` | never satisfied |

**Practical effect today:**

- `conv_not_headon` ≈ `converging` for almost all converging pairs.
- `headon` is effectively always **false**.
- Right-of-way and safety theorems that depend on these predicates are shaped by this permissive behaviour, not by the intended “180° ± Theta_h” geometry.

### Coordinate convention

- **y** = north component  
- **x** = east component  
- Track comment in spec: “angle between north and aircraft direction”

---

## Problem 1: Radians vs Degrees

`atan` returns radians (roughly −1.57 … +1.57). Thresholds `180` and `Theta_h` are written as degrees.

Example for `a0: (2,1)`, `a1: (1,2)`:

| Quantity | Radians | Degrees |
|---|---|---|
| `track(a0)` | ≈ 1.107 | ≈ 63.4° |
| `track(a1)` | ≈ 0.464 | ≈ 26.6° |
| `track_delta` | ≈ 0.643 | ≈ 36.9° |

**Units fix (minimum):** compare like with like — either convert track delta to degrees, or convert thresholds to radians:

```text
track_delta_deg = abs(track(a0) - track(a1)) * 180 / π

# OR

180_rad = π
Theta_h_rad = Theta_h * π / 180
```

This fixes the numeric scale but **does not** fix opposite-heading detection.

---

## Problem 2: `atan(vx/vy)` Loses Direction

`atan` uses the ratio `vx/vy` only. Opposite velocity vectors can yield the **same** track.

| Velocity | `atan(vx/vy)` | True heading |
|---|---|---|
| `(2, 1)` | ≈ +63.4° | northeast |
| `(-2, -1)` | ≈ +63.4° | southwest (opposite) |

Then:

```text
abs(track(a0) - track(a1)) = 0   # should be ≈ 180° for head-on
```

So head-on geometry is missed even after a units fix.

---

## Recommended Fixes

### Option A — Fix `track` with `atan2` (bearing from north)

Use two-argument arctangent for a full directional bearing:

```latex
track\_deg(a) = \mathrm{atan2}(v_x, v_y) \cdot \frac{180}{\pi}
```

Normalize to `[0, 360)` if negative.

For head-on, use **shortest angular distance** between bearings:

```latex
track\_delta = \min\bigl(|t_0 - t_1|,\; 360 - |t_0 - t_1|\bigr)
```

```text
headon(a0, a1)  ⇔  converging(a0, a1)
                   ∧ track_delta ≈ 180° ± Theta_h

conv_not_headon ⇔  converging(a0, a1) ∧ ¬ headon(a0, a1)
```

**Example (fixed):**

| Velocity | `atan2(vx, vy)` in degrees |
|---|---|
| `(2, 1)` | ≈ 63.4° |
| `(-2, -1)` | ≈ 243.4° |
| `track_delta` | ≈ 180° → head-on ✓ |

**Use when:** a standalone `track(a)` function is needed (e.g. `onTrack` EACS tests, display, logging).

---

### Option B — Angle between velocity vectors (recommended for head-on)

Head-on is defined by **how opposite two velocity directions are**, not by subtracting two scalar track values.

```latex
\cos\theta = \frac{v_0 \cdot v_1}{\|v_0\|\,\|v_1\|}
```

```latex
\theta\_deg = \arccos(\cos\theta) \cdot \frac{180}{\pi}
```

```text
headon(a0, a1)  ⇔  converging(a0, a1)
                   ∧ (180 - Theta_h <= theta_deg <= 180 + Theta_h)

conv_not_headon ⇔  converging(a0, a1) ∧ ¬ headon(a0, a1)
```

**Advantages:**

- Symmetric: `θ(a0, a1) = θ(a1, a0)`
- No 0°/360° wrap-around issues
- Directly matches spec intent: “tracks within Theta_h of **180° apart**”
- Reuses existing `dot_product` and `magnitude` (Kazuki/VDM already have these)

**Mapping to existing helpers (conceptual):**

```text
orientation(a0, a1) = dot(v0, v1)     # proportional to cos(theta)
theta_deg = acos( orientation / (|v0| * |v1|) ) * 180/π
```

Clamp cosine to `[-1, 1]` before `acos` for numerical stability.

**Use when:** defining `headon` and `conv_not_headon` (preferred).

---

## Comparison

| Approach | Full bearing? | Detects opposite directions? | Good for `track(a)`? | Good for head-on? |
|---|---|---|---|---|
| `atan(vx/vy)` (current) | No | **No** | Poor | Poor |
| `deg(atan2(vx, vy))` + shortest delta | Yes | Yes | **Good** | Good |
| `angle_between(v0, v1)` | N/A (pairwise) | Yes | N/A | **Best** |

---

## Suggested Specification Strategy

A clean split:

| Function | Recommended definition |
|---|---|
| `track(a)` | Option A: `deg(atan2(vx, vy))` normalized to `[0, 360)` |
| `headon(a0, a1)` | Option B: `angle_between(v0, v1) ≈ 180° ± Theta_h` |
| `conv_not_headon(a0, a1)` | `converging ∧ ¬ headon` |

Avoid `abs(track(a0) - track(a1))` for head-on unless Option A’s shortest angular distance is used consistently in **degrees**.

---

## Impact on Implementations

### Files to update (when implemented)

| Implementation | File |
|---|---|
| VDM | `right-of-way/adapter-vdm/src/main/vdm/RightOfWayRules.vdmsl` |
| Kazuki | `right-of-way/kazuki/src/main/kotlin/.../RightOfWay.kt` |
| SampleImpl | `right-of-way/implementation-V1/src/main/kotlin/.../AirspaceV1.kt` |

### Predicates affected

- `track`
- `headon`
- `conv_not_headon`
- `right_of_way` (converging-not-head-on branch)
- Safety theorems using `conv_not_headon` (e.g. `thm5_mutual_awareness`)

### Predicates **not** affected

- `QC`, `converging`, `HMD`, `tCPA`
- Quadrant geometry (`Q1`–`Q4`)
- `defaultPairConvergence` fixture logic (QC + HMD only)

### EACS tests likely to need review

- `defaultPairHeadOn` — may start passing/failing meaningfully once `headon` works
- `defaultPairOnTrack` — expected angle values depend on `track` definition (degrees vs radians)
- Any scenario relying on `conv_not_headon` tightening after fix

---

## Type / Invariant Notes

- `Angle` in Kazuki: `isPReal(r) && r <= 360.0` — implies **degrees** for angle values exposed to tests.
- `Velocity` invariant: both `vx` and `vy` are non-zero (`NZReal`). This avoids `vy = 0` singularities in `atan(vx/vy)` but **does not** fix the opposite-direction problem.
- VDM `Angle = preal` with `a < 360` — same degree interpretation.

After fix, ensure `track`, `Theta_h`, and head-on comparisons all use **degrees** consistently.

---

## Validation Checklist (post-change)

- [ ] `track(a)` returns values in `[0, 360)` degrees for representative velocities
- [ ] Opposite velocities `(vx, vy)` and `(-vx, -vy)` yield ~180° angle between directions
- [ ] Parallel same-direction velocities yield ~0° angle between directions
- [ ] `headon` true for converging pairs with ~180° velocity separation (within `Theta_h`)
- [ ] `conv_not_headon` false when `headon` true
- [ ] VDM, Kazuki, SampleImpl agree on `headon` / `conv_not_headon` for shared fixtures
- [ ] Re-run `RelativeGeometry` and head-on scenarios with VDM enabled (`impl.includes` includes VDM)
- [ ] Review safety theorems / open-airspace fixtures after `conv_not_headon` semantics tighten

---

## References

- Original track formula: `track(a) = tan^{-1}(v_x / v_y)`
- Current VDM: `RightOfWayRules.vdmsl` — `track`, `headon`, `conv_not_headon`
- Current Kazuki: `right-of-way/kazuki/.../RightOfWay.kt`
- Paper / model context: [Springer chapter 10.1007/978-3-031-20872-0_4](https://link.springer.com/chapter/10.1007/978-3-031-20872-0_4)

---

## Revision History

| Date | Author | Notes |
|---|---|---|
| 2026-06-19 | Design note (AI-assisted) | Initial draft from EACS convergence / head-on analysis |
