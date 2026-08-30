"""Centralized domain mapping for Cogniva cognitive training games.

The 5 cognitive domains supported by Model 1 are:
- memory
- attention
- recognition
- routine
- pattern
"""

DOMAINS = [
    "memory",
    "attention",
    "recognition",
    "routine",
    "pattern",
]

# Mapping from game titles / activity names to standard cognitive domains
GAME_TO_DOMAIN_MAP = {
    # Recognition games
    "who's who?": "recognition",
    "who's who? (face recognition)": "recognition",
    "whos_who": "recognition",
    "face recognition": "recognition",
    "family recognition": "recognition",
    "recognition": "recognition",
    
    # Memory games
    "where was it?": "memory",
    "where was it? (hometown & places)": "memory",
    "where_was_it": "memory",
    "places recall": "memory",
    "hometown recall": "memory",
    "memory": "memory",
    
    # Attention games
    "name that tune": "attention",
    "name that tune (melody recall)": "attention",
    "name_that_tune": "attention",
    "music recall": "attention",
    "melody attention": "attention",
    "attention": "attention",
    
    # Routine games
    "memory talk": "routine",
    "memory talk (photo reminiscence)": "routine",
    "memory_talk": "routine",
    "photo reminiscence": "routine",
    "daily routine": "routine",
    "routine": "routine",
    
    # Pattern games
    "pattern match": "pattern",
    "pattern focus": "pattern",
    "pattern_match": "pattern",
    "pattern": "pattern",
}

def map_game_to_domain(game_name: str | None) -> str:
    """Map an arbitrary game/activity name to one of the 5 standard cognitive domains.
    Defaults to 'memory' if unknown.
    """
    if not game_name:
        return "none"
    cleaned = game_name.strip().lower()
    if cleaned in GAME_TO_DOMAIN_MAP:
        return GAME_TO_DOMAIN_MAP[cleaned]
    # Substring search
    for key, domain in GAME_TO_DOMAIN_MAP.items():
        if key in cleaned:
            return domain
    return "memory"
