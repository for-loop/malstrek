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
STATUS_START_MARKER = "<!-- ADR-STATUSES:START -->"
STATUS_END_MARKER = "<!-- ADR-STATUSES:END -->"
STATUS_DEFINITIONS = (
    ("Proposed", "Under consideration"),
    ("Accepted", "The decision is currently in force"),
    ("Superseded", "Replaced or amended by a later ADR, which should be referenced"),
    ("Deprecated", "No longer applicable"),
)
VALID_STATUSES = {status for status, _ in STATUS_DEFINITIONS}
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


@dataclass(frozen=True)
class AdrMetadata:
    identifier: str
    title: str
    status: str
    summary: str


def parse_adr_metadata(filename, content):
    title_match = TITLE_PATTERN.search(content)
    status_match = STATUS_PATTERN.search(content)
    summary_match = SUMMARY_PATTERN.search(content)

    missing = []
    if title_match is None:
        missing.append("title in '# ADR NNNN: Title' format")
    if status_match is None:
        missing.append("Status field")
    if summary_match is None:
        missing.append("Summary field")
    if missing:
        raise ValueError(f"{filename} is missing " + ", ".join(missing))

    return AdrMetadata(
        identifier=title_match.group(1),
        title=title_match.group(2).strip(),
        status=status_match.group(1).strip(),
        summary=summary_match.group(1).strip(),
    )


def validate_adr_metadata(filename, metadata):
    filename_match = ADR_FILE_PATTERN.fullmatch(filename)
    if filename_match is None:
        raise ValueError(f"{filename} is missing a four-digit ADR filename")

    blank_fields = [
        field
        for field, value in (
            ("title", metadata.title),
            ("Status", metadata.status),
            ("Summary", metadata.summary),
        )
        if not value
    ]
    if blank_fields:
        raise ValueError(f"{filename} has blank fields: " + ", ".join(blank_fields))
    if metadata.status not in VALID_STATUSES:
        raise ValueError(
            f"{filename} has unrecognized status {metadata.status!r}; "
            f"expected one of: {', '.join(status for status, _ in STATUS_DEFINITIONS)}"
        )
    if metadata.identifier != filename_match.group(1):
        raise ValueError(f"{filename} title number does not match filename")


def parse_adr(filename, content):
    metadata = parse_adr_metadata(filename, content)
    validate_adr_metadata(filename, metadata)

    return AdrRecord(
        identifier=metadata.identifier,
        filename=filename,
        title=metadata.title,
        status=metadata.status,
        summary=metadata.summary,
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


def escape_table_cell(value):
    return value.replace("\\", "\\\\").replace("|", "\\|")


def render_catalog(records):
    rows = [
        "| ID | Title | Status | Summary |",
        "|---|---|---|---|",
    ]
    for record in records:
        rows.append(
            f"| [{record.identifier}]({record.filename}) "
            f"| {escape_table_cell(record.title)} | {record.status} "
            f"| {escape_table_cell(record.summary)} |"
        )
    return "\n".join(rows)


def render_status_definitions():
    return "\n".join(f"- **{status}**: {description}" for status, description in STATUS_DEFINITIONS)


def replace_section(content, start_marker, end_marker, section_content):
    if content.count(start_marker) != 1 or content.count(end_marker) != 1:
        raise ValueError("ADR README must contain one pair of section markers")

    before, remainder = content.split(start_marker, 1)
    _, after = remainder.split(end_marker, 1)
    return f"{before}{start_marker}\n{section_content}\n{end_marker}{after}"


def replace_catalog(content, catalog):
    return replace_section(content, START_MARKER, END_MARKER, catalog)


def replace_status_definitions(content):
    return replace_section(
        content,
        STATUS_START_MARKER,
        STATUS_END_MARKER,
        render_status_definitions(),
    )


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
        generated = replace_status_definitions(replace_catalog(current, catalog))
    except (OSError, ValueError) as error:
        print(f"ADR catalog error: {error}", file=sys.stderr)
        return 1

    if check:
        if current != generated:
            print("ADR catalog is out of date; run python3 scripts/check_adr_catalog.py.")
            return 1
        print("ADR catalog is up to date.")
        return 0

    try:
        index_path.write_text(generated, encoding="utf-8")
    except OSError as error:
        print(f"ADR catalog error: {error}", file=sys.stderr)
        return 1

    print(f"Generated {index_path.name}.")
    return 0


def main():
    arguments = parse_arguments()
    return run(arguments.check)


if __name__ == "__main__":
    sys.exit(main())
