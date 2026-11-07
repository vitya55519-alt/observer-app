import re, pathlib, sys
p = pathlib.Path(sys.argv[1])
for m in re.finditer(r'^===FILE (.+?)===\n(.*?)\n===END===', p.read_text(encoding='utf-8'), re.S | re.M):
    f = pathlib.Path(m.group(1))
    f.parent.mkdir(parents=True, exist_ok=True)
    f.write_text(m.group(2) + '\n', encoding='utf-8')
    print(f)
