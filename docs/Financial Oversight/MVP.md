# MVP Plan

Track monthly household income and expenses, calculate real electricity cost after solar offset, and present a trustworthy, auditable financial snapshot with basic budget guardrails.

## Core Financial Model (Required)

This is the non-negotiable foundation.

Entities

Account

Name (e.g. “Household”)

Currency

Transaction

Amount (+ / −)

Category

Date

Source (manual, energy, automation)

Reference ID (for traceability)

Category (fixed set initially)

Income

Fixed costs

Electricity (grid)

Solar (savings / credit)

Maintenance

Other

➡️ Keep categories finite in MVP. Flexibility comes later.

## Monthly Overview Dashboard (Required)

The primary screen users will trust.

Displays

Total income (month)

Total expenses (month)

Net balance

Electricity cost (grid)

Solar offset value

Visuals (simple)

One bar chart (income vs expense)

One line or stacked chart (grid vs solar)

➡️ No drill-downs initially. Clarity over depth.

## Energy → Finance Integration (Core Differentiator)

Minimal, but meaningful.

Inputs

Electricity consumption (kWh)

Solar production (kWh)

Tariff (€/kWh)

Outputs

Grid electricity cost

Solar self-consumption value

Net energy cost for the month

Rules

Solar savings = min(consumption, production) × tariff

Grid cost = (consumption − solar_used) × tariff

➡️ This is deterministic, auditable, and explainable.

## Manual Expense Entry (MVP Level)

Keep it intentionally basic.

Features

Add manual expense/income

Assign category

Optional note

Exclusions (for MVP)

No receipt scanning

No bank sync

No recurring rule engine

➡️ Manual entry = user trust + fast validation.

## Budget Guardrails (Soft Limits Only)

No complex budgeting logic.

Features

Monthly target per category

Simple indicators:

✅ within budget

⚠️ approaching limit

❌ exceeded

➡️ No enforcement, no auto-blocking—just visibility.

## Automation Cost Hooks (Minimal)

Not full automation—just signals.

Events Emitted

MONTHLY_ENERGY_COST_EXCEEDED

SOLAR_SURPLUS_DETECTED

GRID_PRICE_THRESHOLD_EXCEEDED

Purpose

Allow IoT rules later

Enable future optimization

Keep MVP decoupled

➡️ Emit events, don’t act yet.

## Reports & Persistence (Trust Layer)

Essential for adoption.

Reports

Monthly summary

Energy cost breakdown

Export (CSV)

Persistence

Immutable monthly snapshot

Read-only historical months

➡️ Financial data must feel stable and permanent.
