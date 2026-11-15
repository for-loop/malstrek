#!/usr/bin/env python3

import argparse
import re
import sys
from dataclasses import dataclass
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ADR_DIRECTORY = ROOT / "docs" / "adr"
INDEX_PATH = ADR_DIRECTORY / "README.md"
START_MARKER = "<!-- ADR-CATALOG:START -->"
END_MARKER = "<!-- ADR-CATALOG:END -->"
TITLE_PATTERN = re.compile(r"^# ADR (\d{4}):[ \t]*(.*?)[ \t]*$", re.MULTILINE)
STATUS_PATTERN = re.compile(
    r"^(?:[-*][ \t]*)?(?:\*\*)?Status:(?:\*\*)?[ \t]*(.*?)[ \t]*$",
    re.MULTILINE,
)
SUMMARY_PATTERN = re.compile(
    r"^(?:[-*][ \t]*)?(?:\*\*)?Summary:(?:\*\*)?[ \t]*(.*?)[ \t]*$",
    re.MULTILINE,
)
ADR_FILE_PATTERN = re.compile(r"(\d{4})-.+\.md$")
NUMBERED_ADR_FILE_PATTERN = re.compile(r"\d+-.*\.md$")


@dataclass(frozen=True)
class AdrRecord:
    identifier: str
    filename: str
    title: str
    status: str
    summary: str


def parse_adr(filename, content):
    filename_match = ADR_FILE_PATTERN.fullmatch(filename)
    title_match = TITLE_PATTERN.search(content)
    status_match = STATUS_PATTERN.search(content)
    summary_match = SUMMARY_PATTERN.search(content)

    missing = []
    if filename_match is None:
        missing.append("four-digit ADR filename")
    if title_match is None:
        missing.append("title in '# ADR NNNN: Title' format")
    if status_match is None:
        missing.append("Status field")
    if summary_match is None:
        missing.append("Summary field")
    if missing:
        raise ValueError(f"{filename} is missing " + ", ".join(missing))

    title = title_match.group(2).strip()
    status = status_match.group(1).strip()
    summary = summary_match.group(1).strip()
    blank_fields = [
        field
        for field, value in (
            ("title", title),
            ("Status", status),
            ("Summary", summary),
        )
        if not value
    ]
    if blank_fields:
        raise ValueError(f"{filename} has blank fields: " + ", ".join(blank_fields))

    identifier = filename_match.group(1)
    if title_match.group(1) != identifier:
        raise ValueError(f"{filename} title number does not match filename")

    return AdrRecord(
        identifier=identifier,
        filename=filename,
        title=title,
        status=status,
        summary=summary,
    )


def load_adr_records(adr_directory):
    paths = sorted(
        path
        for path in adr_directory.glob("*.md")
        if NUMBERED_ADR_FILE_PATTERN.fullmatch(path.name)
    )
    records = [parse_adr(path.name, path.read_text(encoding="utf-8")) for path in paths]
    identifiers = [record.identifier for record in records]
    if len(identifiers) != len(set(identifiers)):
        raise ValueError("ADR identifiers must be unique")
    return records


def render_catalog(records):
    rows = [
        "| ID | Title | Status | Summary |",
        "|---|---|---|---|",
    ]
    for record in records:
        rows.append(
            f"| [{record.identifier}]({record.filename}) "
            f"| {record.title} | {record.status} | {record.summary} |"
        )
    return "\n".join(rows)


def replace_catalog(content, catalog):
    if content.count(START_MARKER) != 1 or content.count(END_MARKER) != 1:
        raise ValueError("ADR README must contain one pair of catalog markers")

    before, remainder = content.split(START_MARKER, 1)
    _, after = remainder.split(END_MARKER, 1)
    return f"{before}{START_MARKER}\n{catalog}\n{END_MARKER}{after}"


def parse_arguments(arguments=None):
    parser = argparse.ArgumentParser(description="Generate or verify the ADR catalog.")
    parser.add_argument(
        "--check",
        action="store_true",
        help="fail if the ADR catalog in docs/adr/README.md is out of date",
    )
    return parser.parse_args(arguments)


def run(check, adr_directory=ADR_DIRECTORY, index_path=INDEX_PATH):
    try:
        current = index_path.read_text(encoding="utf-8")
        catalog = render_catalog(load_adr_records(adr_directory))
        generated = replace_catalog(current, catalog)
    except (OSError, ValueError) as error:
        print(f"ADR catalog error: {error}", file=sys.stderr)
        return 1

    if check:
        if current != generated:
            print("ADR catalog is out of date; run python3 scripts/check_adr_catalog.py.")
            return 1
        print("ADR catalog is up to date.")
        return 0

    index_path.write_text(generated, encoding="utf-8")
    print(f"Generated {index_path.name}.")
    return 0


def main():
    arguments = parse_arguments()
    return run(arguments.check)


if __name__ == "__main__":
    sys.exit(main())
