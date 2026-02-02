import styles from './Form.module.css'
import PropTypes from 'prop-types'
import Button from '../../../shared/components/Button'

function ImportScheduleForm({ style, previousStepBtn = false, fields = [], formData = {}, onChange, setFormData, onSubmit, mapping}) {
    
    const days = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'];
    const now = new Date();
    const today = now.toISOString().split("T")[0]; // "YYYY-MM-DD" for date inputs
    // Format local datetime for datetime-local input (YYYY-MM-DDTHH:MM)
    const year = now.getFullYear();
    const month = String(now.getMonth() + 1).padStart(2, '0');
    const day = String(now.getDate()).padStart(2, '0');
    const hours = String(now.getHours()).padStart(2, '0');
    const minutes = String(now.getMinutes()).padStart(2, '0');
    const nowDateTimeLocal = `${year}-${month}-${day}T${hours}:${minutes}`;

    const formatDate = (dateString) => {
        if (!dateString) return '';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { 
            month: 'long', 
            day: 'numeric', 
            year: 'numeric',
            hour: 'numeric',
            minute: '2-digit'
        });
    };

    // Validation: check if import name is empty
    const isNameEmpty = !formData.importName || formData.importName.trim() === '';

    // Validation: check if selected datetime is in the past (only when not running immediately)
    const isDateInPast = !formData.immediately && formData.startDate && new Date(formData.startDate) < now;

    // Disable save button if name is empty or date is in the past
    const isSaveDisabled = isNameEmpty || isDateInPast;
    

    return (
        <form className={`${styles['form']}`} style={style} onSubmit={onSubmit}>
            <div className='form-group' style={{order: -1}}>
                <div style={{display: 'flex', gap: '10px', flexWrap: 'wrap', marginBottom: '10px'}}>
                <label><input type="checkbox" name="recurring" checked={formData.recurring || false} onChange={(e) => setFormData({...formData, recurring: e.target.checked})} />
                {' '}Recurring </label>
                    <label><input type="checkbox" name="immediately" checked={formData.immediately || false} onChange={(e) => setFormData({...formData, immediately: e.target.checked})} />Run Immediately</label>
                    <label><input type="checkbox" name="emailNotifications" checked={formData.emailNotifications || false} onChange={(e) => setFormData({...formData, emailNotifications: e.target.checked})} />Email Notifications</label>
                </div>
            </div>

            {fields.map((field, index) => {
    
                // Hide fields based on checkbox states (keep in DOM to prevent layout shift)

                const isStartDateHidden = field.name === 'startDate' && formData.immediately;
                const isEmailHidden = field.name === 'email' && !formData.emailNotifications;
                const isFieldHidden = isStartDateHidden || isEmailHidden;
                const inputType = field.type || 'text';
                
                return (
                    <div 
                        key={field.name || index} 
                        className={styles['form-group']}
                        style={isFieldHidden ? {
                            visibility: 'hidden',
                            height: 0,
                            margin: 0,
                            padding: 0,
                            overflow: 'hidden'
                        } : {}}
                    >
                        <label 
                            htmlFor={field.name} 
                            className={styles['form-group__label']}
                        >
                            {field.label}:
                        </label>
                        <input 
                            id={field.name}
                            name={field.name}
                            value={formData[field.name] || ''}
                            onChange={onChange}
                            required={!isFieldHidden && field.required}
                            className={styles['form-group__input']}
                            disabled={isFieldHidden}
                            type={inputType}
                            {...(inputType === 'date' ? { min: today } : inputType === 'datetime-local' ? { min: nowDateTimeLocal } : {})}
                        />
                    </div>
                );
            })}

            <div className='form-group'>
                <label className='form-group__label'>
                   {formData.recurring && <label style={{ marginRight: '15px'}}><input type="radio" checked={formData.daily || false} onChange={() => setFormData({...formData, daily: true, monthly: false, yearly: false})} name="frequency" value="daily" /> Daily</label>}
                   {formData.recurring && <label style={{ marginRight: '15px'}}><input type="radio" checked={formData.monthly || false} onChange={() => setFormData({...formData, daily: false, monthly: true, yearly: false})} name="frequency" value="monthly" /> Monthly</label>}
                   {formData.recurring && <label style={{ marginRight: '15px'}}><input type="radio" checked={formData.yearly || false} onChange={() => setFormData({...formData, daily: false, monthly: false, yearly: true})} name="frequency" value="yearly" /> Yearly</label>}
                </label>
         
              {formData.daily && formData.recurring && (
                <>
                  <label className='form-group__label'> </label> 
                     <div style={{display: 'flex', gap: '10px', flexWrap: 'wrap'}}>
                    {days.map(day => (
                      <label key={day} style={{display: 'flex', alignItems: 'center', gap: '5px'}}>
                        <input 
                          type="checkbox" 
                          name={day} 
                          checked={formData[day] || false} 
                          onChange={(e) => setFormData({...formData, [day]: e.target.checked})}
                        />
                        {day.charAt(0).toUpperCase() + day.slice(1)}
                      </label>
                    ))}
                     </div>
                </>
              )}

              {formData.monthly && formData.recurring && (
                <div style={{display: 'flex', gap: '10px', flexWrap: 'wrap', marginTop: '20px'}}>
                  <label className='form-group__label'>Day </label>
                  <input type="number" name="dayOfMonth" value={formData.day || ''} onChange={(e) => setFormData({...formData, day: e.target.value})} />
                  <label className='form-group__label'>of every </label>
                  <input type="number" name="monthInterval" value={formData.month || ''} onChange={(e) => setFormData({...formData, month: e.target.value})} />
                  <label className='form-group__label'>month(s)</label>
                </div>
              )}

              {formData.yearly && formData.recurring && (
                <div style={{marginTop: '20px'}}>
                  <label className='form-group__label'>
                    {`Run once a year on ${formatDate(formData.startDate)}`}
                  </label>
                </div>
              )}
            </div>

            <div className={styles['form__button-group']}>
                <Button onClick={previousStepBtn}>Back</Button>
                <Button onClick={() => onSubmit(formData)} disabled={isSaveDisabled}>Save</Button>
            </div>
        </form>
    )
}

ImportScheduleForm.propTypes = {
    children: PropTypes.node,
    style: PropTypes.object,
    nextStepBtn: PropTypes.func,
    previousStepBtn: PropTypes.func,
    onSubmit: PropTypes.func,
    fields: PropTypes.arrayOf(PropTypes.shape({
        name: PropTypes.string.isRequired,
        label: PropTypes.string.isRequired,
        type: PropTypes.string,
        required: PropTypes.bool
    })),
    formData: PropTypes.object,
    onChange: PropTypes.func,
    setFormData: PropTypes.func
}

export default ImportScheduleForm

