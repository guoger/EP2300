#!/usr/bin/python
'''
Title: create plots
Author: Niklas Semmler
'''
import matplotlib.pyplot as plt
import numpy as np
import argparse
from scipy.stats import gaussian_kde
import os

MARKER_SIZE=4
ALPHA_VAL=0.7
WARM_UP_PERIOD = 60
PLOT_PATH = "report/plots"
DEFAULT_PATH = "EP2300-Makeup-CodeTemplate/sim-results/out/result.log"
NUM_PDF_VALUES = 20
NAMES_LIST = ['index', 'actMax', 'estMax', 'estMaxErr', 'actAvg', 'estAvg',
    'estAvgErr', 'maxMsgRate', 'overhead']

def get_color():
    index = -1
    colors = ['bx', 'go', 'rs', 'cd', 'mD', 'y', 'k', 'w']
    while index+1 < len(colors):
        index += 1
        yield colors[index]

params = {
    'actMax':(['x-'], {'markersize':MARKER_SIZE}),
    'estMax':(['o-'], {'linewidth':2, 'markersize':MARKER_SIZE}),
    'estMaxErr':(['s'], {'markersize':MARKER_SIZE}),
    'actAvg':(['-'], {'alpha':0.5, 'linewidth':5, 'markersize':MARKER_SIZE}),
    'estAvg':(['-'], {'linewidth':2, 'markersize':MARKER_SIZE}),
    'estAvgErr':(['d'], {'markersize':MARKER_SIZE})
    #'maxMsgRate':(['b+'], {})
}
names = {}
i = 0

for name in NAMES_LIST:
    names[name] = i
    i += 1

def time_series_plot(ax, data, metadata, val):
    est = 'est' + val
    act = 'act' + val
    ax.grid(True)
    virgin = True
    for name, datum in data.iteritems():
        if virgin:
            time = datum['index']
            plt.plot(time[0:300], datum[act][0:300], *params[act][0], label=act, **params[act][1])
            virgin = False
        plt.plot(time[0:300], datum[est][0:300], metadata[name] + '--', label=name, **params[est][1])
        plt.xlabel("time")
        plt.xlabel(val)
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, labels, numpoints=1)

def trade_off_plot(ax, data, metadata, val):
    ax.grid(True)
    err = 'est' + val + 'Err'
    for name, datum in data.iteritems():
        overhead = datum['overhead']
        plt.plot(datum[err], overhead, metadata[name], label=name, **params[err][1])
    plt.ylabel("overhead")
    plt.xlabel(err)
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, labels, numpoints=1)

def density_plot(ax, data, metadata, val):
    err = 'est' + val + 'Err'
    ax.grid(True)
    for name, datum in data.iteritems():
        datum_wo_warmup = datum[err][WARM_UP_PERIOD:]
        density = gaussian_kde(datum_wo_warmup)
        rangex = np.arange(min(datum_wo_warmup), max(datum_wo_warmup), NUM_PDF_VALUES)
        plt.plot(rangex, density(rangex), metadata[name] + '-', label=name, **params[err][1])
        plt.ylabel("pdf")
        plt.xlabel(err)
    handles, labels = ax.get_legend_handles_labels()
    ax.legend(handles, labels, numpoints=1)

def parse_args(options):
    parser = argparse.ArgumentParser()
    for option in options:
        parser.add_argument(option[0], **option[1])
    return parser.parse_args()

def read_data(input_file):
    data = np.genfromtxt(input_file, delimiter=' ')
    data2 = {}
    for sid in NAMES_LIST:
        data2[sid] = data[:, names[sid]]
    return data2

def read_data_for(task, pattern):
    tasks = ['task1', 'task2', 'task3']
    patterns = ['R1', 'R2']
    all_fnames = []
    def get_fnames(store, dirname, fnames):
        store += fnames

    directory = os.path.join('data', tasks[task-1], patterns[pattern-1])
    os.path.walk(directory, get_fnames, all_fnames)
    data = {}
    metadata = {}
    name = ""
    color = get_color()
    for fname in all_fnames:
        name = fname.split('.')[0].replace('_', '.')
        data[name] = read_data(os.path.join(directory, fname))
        metadata[name] = color.next()

    return data, metadata

def draw_plots(data, metadata):
    fig = plt.figure(1)
    fig.suptitle("time series")
    ax = fig.add_subplot(111)
    time_series_plot(ax, data, metadata, 'Max')
    fig.savefig(os.path.join(PLOT_PATH, "time_series.png"))

    fig = plt.figure(2)
    fig.suptitle("trade off")
    ax = fig.add_subplot(111)
    trade_off_plot(ax, data, metadata, "Max")
    fig.savefig(os.path.join(PLOT_PATH, "trade_off.png"))

    fig = plt.figure(3)
    fig.suptitle("density")
    ax = fig.add_subplot(111)
    density_plot(ax, data, metadata, 'Max')
    fig.savefig(os.path.join(PLOT_PATH, "pdf_of_error.png"))

def main():
    options = [
        ('-f', {'dest':"input_file", 'type':str, 'required':False,
            'help':"locate the log file"}),
        ('-s', {'action':"store_true", 'dest':"only_save",
            'help':"set if you want to save instead of show"}),
        ('-t', {'dest':"task", 'type':int, 'required':True,
            'help':"choose task to plot"}),
        ('-p', {'dest':"pattern", 'type':int, 'required':False,
            'help':"choose to plot"}),
    ]
    args = parse_args(options)
    data, metadata = read_data_for(args.pattern or 1, args.task)
    draw_plots(data, metadata)

    if not args.only_save:
        plt.show()

if __name__ == "__main__":
    path = "/Users/pharaoh/Downloads/first_valid_result.log"
    main()

"""
    TODO: add subplot titles
    TODO: reorder plots for clear comparison of R_1 and R_2
"""
