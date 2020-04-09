from pprint import pprint

slownik = {}

with open("../resources/podsumowanie.txt", "r") as fd:
    lines = fd.read().splitlines()

    for line in lines:

        name, dim, time_ns = line.split(";")
        if dim not in slownik:
            slownik[dim] = {}
        if name not in slownik[dim]:
            slownik[dim][name] = []
        slownik[dim][name].append(int(time_ns))


for dim_key, dim_value in slownik.items():
    for name_key, name_value in dim_value.items():
        avg = sum(name_value)/len(name_value)

        print("%s\tCNT:%d SUM:%d AVG:%.2f MIN:%d MAX:%d"%(
            name_key,
            len(name_value),
            sum(name_value),
            avg,min(name_value),max(name_value))
        )