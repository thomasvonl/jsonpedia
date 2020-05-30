
def convert_data(d):
    return {'link' : '%s %s' % (d['label'], functions.as_text(d['content'])) }

def convert_hr(d):
    return '<a href="%s">%s</a>' % (d['label'], functions.as_text(d['content']))
